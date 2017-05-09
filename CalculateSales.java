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

public class CalculateSales extends Exception {

	public static HashMap<String, String> branchMap = new HashMap<String, String>();
	public static HashMap<String, Long> branchSales = new HashMap<String, Long>();
	public static HashMap<String, String> commodityMap = new HashMap<String, String>();
	public static HashMap<String, Long> commoditySales = new HashMap<String, Long>();
	public static String outputs = null;
	public static FileReader fr = null;
	public static BufferedReader br = null;

	public static void main(String[] args) {


		if (args.length != 1){
		    System.out.println("予期せぬエラーが発生しました");
		    return;
		}

		//支店読み出し
		String dirPath = args[0];
		String fileName = "branch.lst";
		String itemForm = "^[0-9]{3}$";
		HashMap<String, Long> hogeSales = branchSales;
		HashMap<String, String> hogeMap = branchMap;
		String errorMessage = "支店定義ファイルが存在しません";
		String errorMessage2 = "支店定義ファイルのフォーマットが不正です";

		//商品読み出し
		String fileName2 = "commodity.lst";
		String itemForm2 = "^[0-9A-Z]{8}$";
		HashMap<String, Long> hogeSales2 = commoditySales;
		HashMap<String, String> hogeMap2 = commodityMap;
		String errorMessage3 = "商品定義ファイルが存在しません";
		String errorMessage4 = "商品定義ファイルのフォーマットが不正です";
		
		if(!fileReading(dirPath, fileName, itemForm, hogeSales, hogeMap, errorMessage, errorMessage2) || !fileReading(dirPath, fileName2, itemForm2, hogeSales2, hogeMap2, errorMessage3, errorMessage4)) {
			return;
		}


		File sales = new File(args[0]);

		File[] dirLists = sales.listFiles();

		ArrayList<File> rcdList = new ArrayList<File>();

		for (File rcdFile : dirLists) {
			if (rcdFile.isFile() && rcdFile.getName().matches("^\\d{8}.rcd")) {
				rcdList.add(rcdFile);
			}
		}

		for (int i = 0; i < rcdList.size(); i++) {
			int rcdNumber = Integer.parseInt(rcdList.get(i).getName().substring(0,8));

			if(rcdNumber != i+1){
				System.out.println("売上ファイル名が連番になっていません");
				return;
			}
		}

		for (int i = 0; i < rcdList.size(); i++) {

			File files = rcdList.get(i);

			ArrayList<String> InfoList = new ArrayList<String>();

			try {
				fr = new FileReader(files);
				br = new BufferedReader(fr);

				String ss;
				while ((ss = br.readLine()) != null) {
					InfoList.add(ss);
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

			if(InfoList.size() != 3) {
				System.out.println(rcdList.get(i).getName()+"のフォーマットが不正です");
				return;
			}

			if(!branchSales.containsKey(InfoList.get(0))) {
				System.out.println(rcdList.get(i).getName()+"の支店コードが不正です");
				return;
			}

			if(!commoditySales.containsKey(InfoList.get(1))) {
				System.out.println(rcdList.get(i).getName()+"の商品コードが不正です");
				return;
			}

			try {
				Long.parseLong(InfoList.get(2));

			} catch (NumberFormatException e) {
				System.out.println("予期せぬエラーが発生しました");
				return;
			}

			long infoSal = Long.parseLong(InfoList.get(2));
			String infoBranch = InfoList.get(0);
			String infoCommodity = InfoList.get(1);

			long totalSalesB = (branchSales.get(infoBranch)) + infoSal;
			long totalSalesC = (commoditySales.get(infoCommodity)) + infoSal;

			branchSales.put(infoBranch, totalSalesB);
			commoditySales.put(infoCommodity, totalSalesC);

			if(branchSales.get(infoBranch) > 9999999999L || commoditySales.get(infoCommodity) > 9999999999L) {
			System.out.println("合計金額が10桁を超えました");
			return;

			}
		}

		//書き出し
			String fileName3 = "branch.out";
			String fileName4 = "commodity.out";

			if(!fileWriting(dirPath,fileName3,hogeSales,hogeMap) || !fileWriting(dirPath,fileName4,hogeSales2,hogeMap2)) {
				return;
			}
		}

	public static boolean fileReading(String dirPath, String fileName, String itemForm, HashMap<String, Long> hogeSales, HashMap<String, String> hogeMap, String notfoundError, String formatError) {

		File file = new File(dirPath,fileName);

		if(!file.exists()) {
			System.out.println(notfoundError);
			return false;
		}

		try {
			fr = new FileReader(file);
			br = new BufferedReader(fr);

			String s;
			while ((s = br.readLine()) != null) {
				String[] items = s.split(",");
				if (items[0].matches(itemForm) && items.length == 2) {
					hogeMap.put(items[0], items[1]);
					hogeSales.put(items[0], 0L);

				} else {
					System.out.println(formatError);
					return false;
				}
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

	public static boolean fileWriting(String dirPath, String fileName, HashMap<String, Long> hogeSales, HashMap<String, String> hogeMap) {

		BufferedWriter bw = null;

		try{

			File bo = new File(dirPath,fileName);
			bw = new BufferedWriter(new FileWriter(bo));

			List<Map.Entry<String,Long>> array = new ArrayList<Map.Entry<String,Long>>(hogeSales.entrySet());
			Collections.sort(array, new Comparator<Map.Entry<String,Long>>(){

				public int compare(
					Entry<String,Long> entry1, Entry<String,Long> entry2) {
					return ((Long)entry2.getValue()).compareTo((Long)entry1.getValue());
					}
				});

			for (Entry<String,Long> s : array) {

				bw.write(s.getKey() +","+ hogeMap.get(s.getKey()) +","+ s.getValue() + "\n");
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
