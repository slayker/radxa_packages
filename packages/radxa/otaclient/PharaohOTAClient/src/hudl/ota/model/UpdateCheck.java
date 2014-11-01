package hudl.ota.model;

import java.io.Serializable;

import org.codehaus.jackson.annotate.JsonProperty;


/*
 * <pre>
 {
 "status": "ok",
 "update": 
 {
 "name": "Pharaoh", 
 "url": "http://192.168.153.136/system/otaupdate/Pharaoh/V00.01.00/update.zip",
 "file_md5": "f90e6b45a4e46f5037f8814967a2ec05", 
 "datetime_released": "2012-08-29 20:12:41.000000000 +0800", 
 "version": "V00.02.00", 
 "file_size": "3MB", 
 "description": "Please put all of the description for this version here."
 }
 }
 </pre>
 */

@SuppressWarnings("serial")
public class UpdateCheck implements Serializable {
	@JsonProperty
	private String status;
	
	@JsonProperty("update")
	private UpdateInfo updateInfo;
	
	public String getStatus() {
		return status;
	}
	public UpdateInfo getUpdateInfo() {
		return updateInfo;
	}

	
}
