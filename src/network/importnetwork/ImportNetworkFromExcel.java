/* --------------------------------------------------------------------
 * ImportNetworkFromExcel.java
 * --------------------------------------------------------------------
 * (C) Copyright 2015-2016, by Huijun Liu and all Contributors.
 *
 * Original Author:  Huijun Liu
 * Contributor(s):   Qinrui Tang
 *
 * Last Change Date: 05.04.2016
 * 
 * Function:
 *           1.导入Excel格式的路网
 *           2.将路网信息存入矩阵当中
 *           
 */
package network.importnetwork;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.FileInputStream;
import java.io.BufferedInputStream;
import java.util.Iterator;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;

import org.ujmp.core.objectmatrix.DenseObjectMatrix2D;
import org.ujmp.core.objectmatrix.impl.DefaultDenseObjectMatrix2D;

public class ImportNetworkFromExcel
{
	//~Variables
	//--------------------------------------------------------------------------
	
	//~Methods
	//--------------------------------------------------------------------------	
	/**
	 * ImportFromXLS
	 * @param filename
	 * @param sheetNumber
	 * @return
	 * @throws InvalidFormatException
	 * @throws IOException
	 */
	public static DenseObjectMatrix2D importFromXLS(final String filename, final int sheetNumber)
			throws InvalidFormatException, IOException 
	{
		return importFromXLS(new File(filename), sheetNumber);
	}
	
	/**
	 * ImportFromXLS
	 * 
	 * @param file
	 * 		The file which will be imported
	 * @param sheetNumber
	 * 		The Sheet in the workbook which will be imported
	 */
	public static DenseObjectMatrix2D importFromXLS(final File file, final int sheetNumber) 
			throws InvalidFormatException, IOException 
	{
		final FileInputStream fileInputStream = new FileInputStream(file);
		final BufferedInputStream bufferedInputStream = new BufferedInputStream(fileInputStream);
		final DenseObjectMatrix2D matrix = importFromXLS(bufferedInputStream, sheetNumber);
		bufferedInputStream.close();
		fileInputStream.close();
		return matrix;
	}
	
	/**
	 * ImportFromXLS
	 * @param inputStream
	 * @param sheetNumber
	 * @return
	 * @throws InvalidFormatException
	 * @throws IOException
	 */
	public static DenseObjectMatrix2D importFromXLS(final InputStream inputStream, final int sheetNumber)
			throws InvalidFormatException, IOException 
	{
		final HSSFWorkbook workbook = new HSSFWorkbook(inputStream);
		return importFromWorkbook(workbook, sheetNumber);
	}
	
	//====================================================================================
	//
	/**
	 * ImportFromXLSX
	 * 
	 * @param file
	 * 		The file which will be imported
	 * @param sheetNumber
	 * 		The Sheet in the workbook which will be imported
	 */
	public static DenseObjectMatrix2D importFromXLSX(final String filename, final int sheetNumber)
			throws InvalidFormatException, IOException 
	{
		return importFromXLSX(new File(filename), sheetNumber);
	}
	
	/**
	 * ImportFromXLSX
	 * @param file
	 * @param sheetNumber
	 * @return
	 * @throws InvalidFormatException
	 * @throws IOException
	 */
	public static DenseObjectMatrix2D importFromXLSX(final File file, final int sheetNumber) 
			throws InvalidFormatException,IOException 
	{
		final OPCPackage pkg = OPCPackage.open(file);
		final XSSFWorkbook workbook = new XSSFWorkbook(pkg);
		final DenseObjectMatrix2D matrix = importFromWorkbook(workbook, sheetNumber);
		pkg.close();
		
		return matrix;
	}
	
	/**
	 * ImportFromXLSX
	 * @param inputStream
	 * @param sheetNumber
	 * @return
	 * @throws InvalidFormatException
	 * @throws IOException
	 */
	public static DenseObjectMatrix2D importFromXLSX(final InputStream inputStream, final int sheetNumber)
			throws InvalidFormatException, IOException 
	{
		final OPCPackage pkg = OPCPackage.open(inputStream);
		final XSSFWorkbook workbook = new XSSFWorkbook(pkg);
		final DenseObjectMatrix2D matrix = importFromWorkbook(workbook, sheetNumber);
		pkg.close();
		
		return matrix;
	}
	
	/**
	 * ImportFromWorkbook
	 * 
	 * @param workbook
	 * 		The workbook which will be imported
	 * @param sheetNumber
	 * 		The Sheet in the workbook which will be imported
	 */
	public static DenseObjectMatrix2D importFromWorkbook(final Workbook workbook, final int sheetNumber)
			throws InvalidFormatException, IOException 
	{
		final Sheet sheet = workbook.getSheetAt(sheetNumber);
		return importFromSheet(sheet);
	}
	
	/**
	 * ImportFromSheet
	 * 
	 * @param sheet
	 * 		The Sheet which will be imported
	 */
	public static DenseObjectMatrix2D importFromSheet(final Sheet sheet) 
			throws InvalidFormatException, IOException 
	{
		//final int rowCount = sheet.getLastRowNum();
		final int rowCount = sheet.getPhysicalNumberOfRows();
		int columnCount = 0;

		final Iterator<Row> rowIterator = sheet.rowIterator();
		while(rowIterator.hasNext()) 
		{
			final Row row = rowIterator.next();
			if(row.getLastCellNum() > columnCount) 
			{
				columnCount = row.getLastCellNum();
			}
		}
		
		final DefaultDenseObjectMatrix2D matrix = new DefaultDenseObjectMatrix2D(rowCount, columnCount);
		matrix.setLabel(sheet.getSheetName());

		for(int r = 0; r < rowCount; r++) 
		{
			final Row row = sheet.getRow(r);
			if(row != null) 
			{
				for(int c = 0; c < columnCount; c++) 
				{
					final Cell cell = row.getCell(c);
					if(cell != null) 
					{
						switch(cell.getCellType()) 
						{
							case Cell.CELL_TYPE_BLANK:
								break;
							case Cell.CELL_TYPE_BOOLEAN:
								matrix.setAsBoolean(cell.getBooleanCellValue(), r, c);
								break;
							case Cell.CELL_TYPE_ERROR:
								break;
							case Cell.CELL_TYPE_FORMULA:
								matrix.setAsString(cell.getCellFormula(), r, c);
								break;
							case Cell.CELL_TYPE_NUMERIC:
								matrix.setAsDouble(cell.getNumericCellValue(), r, c);
								break;
							case Cell.CELL_TYPE_STRING:
								matrix.setAsString(cell.getStringCellValue(), r, c);
								break;
							default:
								break;
						}

					}
				}
			}
		}

		return matrix;
	}
}
