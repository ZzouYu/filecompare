package com.hurricane.app.file.compare;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class App {
	private static final Logger logger = LoggerFactory.getLogger(App.class);
	public static void main(String[] args) throws IOException {
		long beginTime = System.currentTimeMillis();
		InputStream resourceAsStream = App.class.getClassLoader().getResourceAsStream("config.properties");
		Properties properties = new Properties();
		properties.load(resourceAsStream);
		resourceAsStream.close();
		Object file1Path = properties.get("file1.path");
		Object file1Prefix = properties.get("file1.prefix");
		Object file2Path = properties.get("file2.path");
		Object file2Prefix = properties.get("file2.prefix");
		Object omitFile = properties.get("omit.file.path");
		Objects.requireNonNull(file1Path);
//		Objects.requireNonNull(file1Prefix);
		Objects.requireNonNull(file2Path);
//		Objects.requireNonNull(file2Prefix);
		Objects.requireNonNull(omitFile);
		//没有上传成功的数据列表
		List<String> omittedList = new ArrayList<String>();
		
		//装载本地数据信息
		List<String> localList = new ArrayList<String>();
		InputStream inputStream = new FileInputStream(file1Path.toString());
		BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream,"UTF-8"));
		BufferedReader bufferedReader = new BufferedReader(reader);
		String line;
		while ((line=bufferedReader.readLine())!=null) {
			if (file1Prefix==null||file1Prefix.equals("")){
				localList.add(line);
			}
			else{
				System.out.println(line);
				System.out.println(file1Prefix);
				System.out.println(line.substring(file1Prefix.toString().length()));
				System.out.println(line.indexOf(file1Prefix.toString()));
				localList.add(line.substring(file1Prefix.toString().length()));
				System.out.println(line.substring(file1Prefix.toString().length()));
			}
		}
		reader.close();
		//装载OSS云端数据
		List<String> ossList = new ArrayList<String>();
		InputStream inputStream2 = new FileInputStream(file2Path.toString());
		BufferedReader reader2 = new BufferedReader(new InputStreamReader(inputStream2,"UTF-8"));
		BufferedReader bufferedReader2 = new BufferedReader(reader2);
		while ((line=bufferedReader2.readLine())!=null) {
			if (file2Prefix==null||file2Prefix.equals("")) {
				ossList.add(line);
				System.out.println(line.substring(file2Prefix.toString().length()));				
			}
			else{
				ossList.add(line.substring(file2Prefix.toString().length()+1));
			}
		}
		
		//比较两个数据集
		for (String string : localList) {
			boolean contains = ossList.contains(string);
			if (!contains) {
				omittedList.add(string);
				logger.debug("发现未成功上传数据:{}.",string);
			}
		}
		
		if (omittedList.size()>0) {
			logger.warn("共发现未上传数据{}条，导出到{}.",omittedList.size(), omitFile);
			logger.warn("导出开始");
			OutputStream outputStream = new FileOutputStream(omitFile.toString());
			for (String string : omittedList) {
				outputStream.write(string.getBytes());
				outputStream.write("\r\n".getBytes());
			}
			outputStream.flush();
			outputStream.close();
			logger.warn("导出结束");
		}else {
			logger.warn("未发现上传失败的数据");
		}
		
		logger.info("程序运行结束，共运行{}ms.",System.currentTimeMillis()-beginTime);
	}
}
