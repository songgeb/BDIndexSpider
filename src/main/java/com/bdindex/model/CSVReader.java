package com.bdindex.model;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.io.input.BOMInputStream;

@InputType("csv")
public class CSVReader extends ArrayList<String> implements Reader {

	private static final long serialVersionUID = -1782900543740030723L;


	private Iterable<CSVRecord> records;

	private int modelNum;

	public CSVReader(File file) {
		read(file);
	}

	@Override
	public void read(File file) {
		readCsv(file);
	}

	public enum Headers {
		keyword, startDate, endDate
	}

	private void readCsv(File file) {
		// 利用apache commons csv
		java.io.Reader reader = null;
		try {
			// 处理bom标记
			reader = new InputStreamReader(new BOMInputStream(file.toURI()
					.toURL().openStream()), "UTF-8");
			this.records = CSVFormat.EXCEL.withHeader(Headers.class).parse(
					reader);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public ArrayList<HashMap<String, String>> generate() {
		ArrayList<HashMap<String, String>> res = new ArrayList<HashMap<String, String>>();
		HashMap<String, String> argMap = null;
		for (CSVRecord record : records) {
			argMap = (HashMap<String, String>)record.toMap();
			if (!Objects.equals(argMap.size(), 3)) {
				throw new IllegalArgumentException(String.valueOf(modelNum) + 1);
			}
			modelNum++;
			res.add(argMap);
		}
		return res;
	}

	

	public static void main(String[] args) throws IOException,
			URISyntaxException {
		File file = new File("src/main/resources/test.csv");
		CSVReader csvReader = new CSVReader(file);
		csvReader.generate();
	}

}
