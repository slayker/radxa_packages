package hudl.ota.model;

import hudl.ota.util.Util;

import org.codehaus.jackson.annotate.JsonProperty;


import android.os.Parcel;
import android.os.Parcelable;

/*
 * 
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
 */

@SuppressWarnings("serial")
public class UpdateInfo implements Parcelable {

	public UpdateInfo() {

	}

	public UpdateInfo(Parcel in) {

		String[] data = new String[10];

		in.readStringArray(data);

		this.name = data[0]; // first position
		this.description = data[1]; // second position
		this.size = data[2]; // third position
		this.mandatory = Boolean.valueOf(data[3]); // fourth position
		this.currentAndroidVersion = data[4]; // fifth position
		this.terms = data[5]; // sixth position,
		this.timestamp = data[6];
		this.updateChecksum = data[7];
		this.updateURL = data[8];
		this.version = data[9];

	}

	public UpdateInfo(String flattenString) {

		try {

			String[] set = flattenString.split("%");

			if (set != null) {

				this.name = set[0];
				this.description = set[1]; // second position
				this.size = set[2]; // third position
				this.mandatory = Boolean.getBoolean(set[3]); // fourth
																// position
				this.currentAndroidVersion = set[4]; // fifth position
				this.terms = set[5]; // sixth position,
				this.timestamp = set[6];
				this.updateChecksum = set[7];
				this.updateURL = set[8];
				this.version = set[9];
			} else {

				throw new NullPointerException("Set String is null");
			}
		} catch (Exception e) {
			Util.log(e);
		}

	}

	@JsonProperty
	private String currentAndroidVersion;
	@JsonProperty
	private boolean mandatory = true;

	@JsonProperty
	private String name;

	@JsonProperty("file_md5")
	private String updateChecksum;

	@JsonProperty("url")
	private String updateURL;

	@JsonProperty("file_size")
	private String size;

	@JsonProperty("version")
	private String version;

	@JsonProperty("description")
	private String description;

	@JsonProperty("datetime_released")
	private String timestamp;

	@JsonProperty
	private String terms;

	public boolean isMandatory() {
		return mandatory;
	}

	public String getUpdateChecksum() {
		return updateChecksum;
	}

	public String getUpdateURL() {
		return updateURL;
	}

	public String getSize() {
		return size;
	}

	public String getVersion() {
		return version;
	}

	public String getDescription() {
		return description;
	}

	public String getTimestamp() {
		return timestamp;
	}

	/*
	 * Paranoid check on fields
	 */
	public boolean isValid() {
		boolean valid = true;

		if (description == null || description.trim().length() == 0) {
			valid = false;
		}

		// to be continued...

		return valid;
	}

	public String getTerms() {
		return terms;
	}

	public void setTerms(String terms) {
		this.terms = terms;
	}

	public String writeToString() {

		String flattenString = this.name + "%" + this.description + "%" + this.size + "%"
				+ Boolean.toString(this.mandatory) + "%" + this.currentAndroidVersion + "%" + this.terms + "%"
				+ this.timestamp + "%" + this.updateChecksum + "%" + this.updateURL + "%" + this.version;

		return flattenString;

	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {

		String[] arrayUpdateInfo = new String[] { this.name, // first position
				this.description, // second position
				this.size, // third position
				Boolean.toString(this.mandatory), // fourth position
				this.currentAndroidVersion, // fifth position
				this.terms, // sixth position,
				this.timestamp, this.updateChecksum, this.updateURL, this.version };

		dest.writeStringArray(arrayUpdateInfo);
	}

	@Override
	public int describeContents() {

		return 0;
	}

	public static final Parcelable.Creator CREATOR = new Parcelable.Creator() {
		public UpdateInfo createFromParcel(Parcel in) {
			return new UpdateInfo(in);
		}

		public UpdateInfo[] newArray(int size) {
			return new UpdateInfo[size];
		}
	};
}
