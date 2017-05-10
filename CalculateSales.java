package jp.alhinc.aibara_saki.calculate_sales;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class CalculateSales {

	public static HashMap<String, String> branchMap = new HashMap<>();
	public static HashMap<String, Long> branchSales = new HashMap<>();
	public static HashMap<String, String> commodityMap = new HashMap<>();
	public static HashMap<String, Long> commoditySales = new HashMap<>();
	public static FileReader fr = null;
	public static BufferedReader br = null;

	public static void main(String[] args) {


		if (args.length != 1) {
		    System.out.println("予期せぬエラーが発生しました");
		    return;
		}

		String dirPath = args[0];

		//支店,商品読み出し
		if (!fileReading(dirPath, "branch.lst", "^[0-9]{3}$", branchSales, branchMap, "支店") ||
			!fileReading(dirPath, "commodity.lst", "^[0-9a-zA-Z]{8}$", commoditySales, commodityMap, "商品")) {
			return;
		}

		File sales = new File(dirPath);

		File[] dirLists = sales.listFiles();

		ArrayList<File> rcdList = new ArrayList<>();

		for (File rcdFile : dirLists) {
			if (rcdFile.isFile() && rcdFile.getName().matches("^\\d{8}.rcd")) {
				rcdList.add(rcdFile);
			}
		}

		for (int i = 0; i < rcdList.size(); i++) {
			int rcdNumber = Integer.parseInt(rcdList.get(i).getName().substring(0,8));

			if (rcdNumber != i + 1){
				System.out.println("売上ファイル名が連番になっていません");
				return;
			}
		}

		for (int i = 0; i < rcdList.size(); i++) {

			File files = rcdList.get(i);

			ArrayList<String> infoList = new ArrayList<>();

			try {
				fr = new FileReader(files);
				br = new BufferedReader(fr);

				String ss;
				while ((ss = br.readLine()) != null) {
					infoList.add(ss);
				}

			} catch (IOException e) {
				System.out.println("予期せぬエラーが発生しました");
				return;

			} finally {
				try {
					if (br != null) {
					br.close();
					}
				} catch (IOException e) {
					System.out.println("予期せぬエラーが発生しました");
				}
			}

			if (infoList.size() != 3) {
				System.out.println(rcdList.get(i).getName() + "のフォーマットが不正です");
				return;
			}

			if (!branchSales.containsKey(infoList.get(0))) {
				System.out.println(rcdList.get(i).getName() + "の支店コードが不正です");
				return;
			}

			if (!commoditySales.containsKey(infoList.get(1))) {
				System.out.println(rcdList.get(i).getName() + "の商品コードが不正です");
				return;
			}

			if (!infoList.get(2).matches("^[0-9]{1,}$")) {
				System.out.println("予期せぬエラーが発生しました");
				return;
			}

			long infoSales = Long.parseLong(infoList.get(2));
			String infoBranch = infoList.get(0);
			String infoCommodity = infoList.get(1);

			long branchTotal = (branchSales.get(infoBranch)) + infoSales;
			long commodityTotal = (commoditySales.get(infoCommodity)) + infoSales;

			if (branchTotal > 9999999999L || commodityTotal > 9999999999L) {
				System.out.println("合計金額が10桁を超えました");
				return;
			}

			branchSales.put(infoBranch, branchTotal);
			commoditySales.put(infoCommodity, commodityTotal);

		}

		//書き出し
		if (!fileWriting(dirPath, "branch.out", branchSales, branchMap) ||
			!fileWriting(dirPath, "commodity.out", commoditySales, commodityMap)) {
			return;
		}
	}

	public static boolean fileReading(String dirPath, String fileName, String fileNameForm, HashMap<String,Long> bcSales, HashMap<String,String> bcMap, String bc) {

		File file = new File(dirPath, fileName);

		if (!file.exists()) {
			System.out.println(bc + "定義ファイルが存在しません");
			return false;
		}

		try {
			fr = new FileReader(file);
			br = new BufferedReader(fr);

			String s;
			while ((s = br.readLine()) != null) {
				String[] items = s.split(",");
				if (items.length != 2 || !items[0].matches(fileNameForm)) {
					System.out.println(bc + "定義ファイルのフォーマットが不正です");
					return false;
				}
				bcMap.put(items[0],items[1]);
				bcSales.put(items[0],0L);
			}

		} catch (IOException e) {
			System.out.println("予期せぬエラーが発生しました");
			return false;

		} finally {
			try {
				if (br != null) {
					br.close();
				}
			} catch (IOException e) {
				System.out.println("予期せぬエラーが発生しました");
				return false;
			}
		}
		return true;
	}

	public static boolean fileWriting(String dirPath, String fileName, HashMap<String, Long> bcSales, HashMap<String, String> bcMap) {

		BufferedWriter bw = null;

		try {

			File bo = new File(dirPath, fileName);
			bw = new BufferedWriter(new FileWriter(bo));

			List<Map.Entry<String, Long>> array = new ArrayList<Map.Entry<String, Long>>(bcSales.entrySet());
			Collections.sort(array,new Comparator<Map.Entry<String,Long>>(){

				public int compare(
					Entry<String, Long> entry1, Entry<String, Long> entry2) {
					return ((Long)entry2.getValue()).compareTo((Long)entry1.getValue());
					}
				});

			for (Entry<String, Long> s : array) {

				bw.write(s.getKey() + "," + bcMap.get(s.getKey()) + "," + s.getValue());
				bw.newLine();
			}

		} catch (IOException e) {
			System.out.println("予期せぬエラーが発生しました");
			return false;

		} finally {
			try {
				if (bw != null) {
					bw.close();
				}
			} catch (IOException e) {
				System.out.println("予期せぬエラーが発生しました");
				return false;
			}
		}
		return true;

	}
}