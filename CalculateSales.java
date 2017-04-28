package jp.alhinc.saki_aibara.calculate_sales;

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

	public static void main(String[] args) {

		File file = new File(args[0] + "branch.lst");

		FileReader fr = null;
		BufferedReader br = null;
		
		if(!file.exists()) {
			System.out.println("支店定義ファイルが存在しません");
			return;
		}
		
		try {
			fr = new FileReader(file);
			br = new BufferedReader(fr);

			String s;
			while ((s = br.readLine()) != null) {
				String[] items = s.split(",");
				if (items[0].matches("^[0-9]{3}$") && items.length == 2) {
					branchMap.put(items[0], items[1]);
					branchSales.put(items[0], 0L);

				

				} else {
					System.out.println("支店定義ファイルのフォーマットが不正です");
					return;
				}
			}

		} catch (IOException e) {
			System.out.println("予期せぬエラーが発生しました");

		} finally {
			try {
				if (br != null) {
					br.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}


		File file2 = new File(args[0] + "commodity.lst");
		
		if(!file2.exists()) {
			System.out.println("商品定義ファイルが存在しません");
			return;
		}


		try {

			fr = new FileReader(file2);
			br = new BufferedReader(fr);

			String s2;
			while ((s2 = br.readLine()) != null) {
				String[] items = s2.split(",");

				if (items[0].matches("^[0-9A-Z]{8}$") && items.length == 2) {
					commodityMap.put(items[0], items[1]);
					commoditySales.put(items[0], 0L);

				} else {
					System.out.println("商品定義ファイルのフォーマットが不正です");
					return;
				}
			}

		} catch (IOException e) {
			System.out.println("予期せぬエラーが発生しました");
		}


		File sales = new File(args[0]);

		File[] dirLists = sales.listFiles();

		ArrayList<File> rcdList = new ArrayList<File>();

		for (File rcdFile : dirLists) {
			if (rcdFile.getName().matches("^\\d{8}.rcd")) {

				rcdList.add(rcdFile);
			}
		}

		for (int i = 0; i < rcdList.size(); i++) {
			File file3 = rcdList.get(i);

			int rcdNumber = Integer.parseInt(rcdList.get(i).getName().substring(0,8));

			if(rcdNumber != i+1){
				System.out.println("売上ファイル名が連番になっていません");
				return;
			}

			ArrayList<String> InfoList = new ArrayList<String>();

			try {

				fr = new FileReader(file3);
				br = new BufferedReader(fr);

				String ss;
				while ((ss = br.readLine()) != null) {
					InfoList.add(ss);
			}

			} catch (IOException e) {
				System.out.println("予期せぬエラーが発生しました");
			}


			if (args.length != 1){
			    System.out.println("予期せぬエラーが発生しました");
			    return;
			}

			if(!branchSales.containsKey(InfoList.get(0))) {
				System.out.println("<"+rcdList.get(i).getName()+">の支店コードが不正です");
				return;
			}

			if(!commoditySales.containsKey(InfoList.get(1))) {
				System.out.println("<"+rcdList.get(i).getName()+">の商品コードが不正です");
				return;
			}

			if(InfoList.size() != 3) {
				System.out.println("<"+rcdList.get(i).getName()+">のフォーマットが不正です" + InfoList.size());
				return;
			}

			if(InfoList.size() != 3) {
				System.out.println("<"+rcdList.get(i).getName()+">のフォーマットが不正です" + InfoList.size());
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
			String fileName = args[0] + "\\branch.out";
			HashMap<String, Long> hogeSales = branchSales;
			HashMap<String, String> hogeMap = branchMap;
			fileWriting(fileName,hogeSales,hogeMap);

			fileName = args[0] + "\\commodity.out";
			hogeSales = commoditySales;
			hogeMap = commodityMap;
			fileWriting(fileName,hogeSales,hogeMap);
	}


	public static void fileWriting(String output, HashMap<String, Long> hogeSales, HashMap<String, String> hogeMap) {

		BufferedWriter bw =null;

		try{
			File bo = new File(output);
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
			return;

		} finally {
			try {
				if (bw != null) {
					bw.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
				System.out.println("予期せぬエラーが発生しました");
			}
		}
	}
}