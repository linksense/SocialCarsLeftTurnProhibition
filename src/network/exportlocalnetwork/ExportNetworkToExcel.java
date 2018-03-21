/* --------------------------------------------------------------------
 * ExportNetworkToExcel.java
 * --------------------------------------------------------------------
 * (C) Copyright 2015-2016, by Huijun Liu and all Contributors.
 *
 * Original Author:  Huijun Liu
 * Contributor(s):   Qinrui Tang
 *
 * Last Change Date: 10.04.2016
 * 
 * Function:
 *           1.导出路网Excel文件
 *           
 */
package network.exportlocalnetwork;

import java.io.File;
import java.util.Date;
import java.io.IOException;
import java.io.FileOutputStream;
import java.io.BufferedOutputStream;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import org.ujmp.core.Matrix;
import org.ujmp.core.util.StringUtil;

public class ExportNetworkToExcel
{
	//~Variables
	//--------------------------------------------------------------------------

	//~Methods
	//--------------------------------------------------------------------------
	/**
	 * 导出矩阵到XLS EXCEL文件
	 * @param matrix
	 * @param filename
	 * @param sheetName
	 * @throws IOException
	 */
	public static void exportToXLSFile(final Matrix matrix, final String filename, 
									   final String sheetName)throws IOException
	{
		exportToXLSFile(matrix, new File(filename), sheetName);
	}
	/**
	 * 导出矩阵到XLS EXCEL文件
	 * @param matrix
	 * @param file
	 * @param sheetName
	 * @throws IOException
	 */
	public static void exportToXLSFile(final Matrix matrix, final File file,
											 String sheetName) throws IOException
	{
		final Workbook workbook = new HSSFWorkbook();
		//调用exportToExcelFile将矩阵方法导出到Excel文件
		exportToExcelFile(workbook, matrix, file, sheetName);

		//final FileOutputStream fileOutputStream = new FileOutputStream(file);
		//final BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(fileOutputStream);
		//workbook.write(fileOutputStream);
		
		//bufferedOutputStream.close();
		//fileOutputStream.close();
	}

	/**
	 * 导出矩阵到XLSX EXCEL文件
	 * @param matrix
	 * @param filename
	 * @param sheetName
	 * @throws IOException
	 */
	public static void exportToXLSXFile(final Matrix matrix, final String filename, 
											  final String sheetName)throws IOException
	{
		exportToXLSXFile(matrix, new File(filename), sheetName);
	}
	/**
	 * 导出矩阵到XLSX EXCEL文件
	 * @param matrix
	 * @param file
	 * @param sheetName
	 * @throws IOException
	 */
	public static void exportToXLSXFile(final Matrix matrix, final File file, 
											  final String sheetName)throws IOException
	{
		final Workbook workbook = new XSSFWorkbook();
		//调用exportToExcelFile将矩阵方法导出到Excel文件
		exportToExcelFile(workbook, matrix, file, sheetName);

		//final FileOutputStream fileOutputStream = new FileOutputStream(file);
		//final BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(fileOutputStream);
		//workbook.write(fileOutputStream);
		
		//bufferedOutputStream.close();
		//fileOutputStream.close();
	}

	/**
	 * 把矩阵导出到EXCEL文件
	 * @param workbook
	 * @param matrix
	 * @param file
	 * @param sheetName
	 * @throws IOException
	 */
	private static void exportToExcelFile(final Workbook workbook, final Matrix matrix,
											final File file, String sheetName) throws IOException
	{
		final Sheet sheet = workbook.createSheet(sheetName);
		
		final int rowCount = (int) matrix.getRowCount();
		final int columnCount = (int) matrix.getColumnCount();
		for(int r = 0; r < rowCount; r++)
		{
			Row row = sheet.createRow(r);
			for(int c = 0; c < columnCount; c++)
			{
				Object obj = matrix.getAsObject(r, c);
				if(obj != null)
				{
					Cell cell = row.createCell(c);
					if(obj instanceof Double)
					{
						cell.setCellValue((Double) obj);
					}
					else if(obj instanceof String)
					{
						cell.setCellValue((String) obj);
					}
					else if(obj instanceof Date)
					{
						cell.setCellValue((Date) obj);
					}
					else if(obj instanceof Boolean)
					{
						cell.setCellValue((Boolean) obj);
					}
					else
					{
						cell.setCellValue(StringUtil.convert(obj));
					}
				}
			}
		}

		final FileOutputStream fileOutputStream = new FileOutputStream(file);
		final BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(fileOutputStream);
		workbook.write(fileOutputStream);
		
		bufferedOutputStream.close();
		fileOutputStream.close();
	}
}
