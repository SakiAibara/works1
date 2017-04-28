package jp.alhinc.saki_aibara.calculate_sales;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
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
	public static void main(String[] args) {
		
		File file = new File(args[0] + "branch.lst");

		HashMap<String, String> branchMap = new HashMap<String, String>();
		HashMap<String, Long> branchSales = new HashMap<String, Long>();

		try {
			FileReader fr = new FileReader(file);
			BufferedReader br = new BufferedReader(fr);

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
			br.close();
		} catch (FileNotFoundException e) {
			System.out.println("支店定義ファイルが存在しません");
		} catch (IOException e) {
			System.out.println("予期せぬエラーが発生しました");
		}
		

		File file2 = new File(args[0] + "commodity.lst");

		HashMap<String, String> commodityMap = new HashMap<String, String>();
		HashMap<String, Long> commoditySales = new HashMap<String, Long>();

		try {
			FileReader fr = new FileReader(file2);
			BufferedReader br = new BufferedReader(fr);

			String s2;
			while ((s2 = br.readLine()) != null) {
				String[] items = s2.split(",");

				if (items[0].matches("^[0-9A-Z]{8}$") && items.length == 2) {
					commodityMap.put(items[0], items[1]);
					commoditySales.put(items[0], 0L);
//					System.out.println(items[0] + "は" + commodityMap.get(items[0]));
				} else {
					System.out.println("商品定義ファイルのフォーマットが不正です");
					return;
				}
			}
			br.close();
		} catch (FileNotFoundException e) {
			System.out.println("商品定義ファイルが存在しません");
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

				FileReader fr = new FileReader(file3);
				BufferedReader br = new BufferedReader(fr);

				String ss;
				while ((ss = br.readLine()) != null) {
					InfoList.add(ss);
			}

			} catch (IOException e) {
				System.out.println("予期せぬエラーが発生しました");
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

		try{
			File bo = new File(args[0] + "branch.out");
			FileWriter fw = new FileWriter(bo);
			BufferedWriter bw = new BufferedWriter(fw);

			List<Map.Entry<String,Long>> array = new ArrayList<Map.Entry<String,Long>>(branchSales.entrySet());
			Collections.sort(array, new Comparator<Map.Entry<String,Long>>(){

				public int compare(
					Entry<String,Long> entry1, Entry<String,Long> entry2) {
					return ((Long)entry2.getValue()).compareTo((Long)entry1.getValue());
					}
				});

			for (Entry<String,Long> s : array) {

				bw.write(s.getKey() +","+ branchMap.get(s.getKey()) +","+ s.getValue() + "\n");
			}
			bw.close();

			
				File co = new File(args[0] + "commodity.out");
				FileWriter fw2 = new FileWriter(co);
				BufferedWriter bw2 = new BufferedWriter(fw2);

				List<Entry<String,Long>> array2 = new ArrayList<Map.Entry<String,Long>>(commoditySales.entrySet());
				Collections.sort(array2, new Comparator<Map.Entry<String,Long>>(){

					public int compare(
						Entry<String,Long> entry3, Entry<String,Long> entry4) {
						return ((Long)entry4.getValue()).compareTo((Long)entry3.getValue());
						}
					});

			for (Entry<String,Long> sc : array2) {

				bw2.write(sc.getKey() +","+ commodityMap.get(sc.getKey()) +","+ sc.getValue() + "\n");

			}
			bw2.close();

		} catch (IOException e) {
			System.out.println("予期せぬエラーが発生しました" + e);
			return;
		}
	}
}