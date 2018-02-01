package com.untzuntz.coredata.export;

public interface ExportFieldFormat {

	public String getHeaderName();
	
	public String getFieldName();

	/**
	 * The data format of this field
	 */
	public String getFieldFormat();
	
	public String getForegroundColor();
	
	public String getBackgroundColor();
	
	public void calculateLineCount(Object data);
	
	public int getLineCount();
	/**
	 * Returns the string value of the data.
	 */
	public Object getFieldValue(Object data, int outputLine);
	
}
