package com.walletv2.filehandler;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

import com.walletv2.entity.Constants;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.Arrays;

public class FileHandler {
	Context context = null;
	private String[] mFileList;
	private final String FTYPE = Constants.DB_NAME;
	private final String sdPath;
	private final String dataDBPath;
	private final String dataSPPath;
	private final String sdCardDBDirectoryPath;
	private final String sdCardSPPath;
	
	public FileHandler(Context context) {
		this.context = context;
		String sharedPrefsFileName = context.getPackageName() + "_preferences" + ".xml";
		String dataPath = context.getApplicationInfo().dataDir;
		sdPath = Environment.getExternalStorageDirectory().toString();
		dataDBPath = dataPath + "/databases/" + Constants.DB_NAME;
		dataSPPath = dataPath + "/shared_prefs/" + sharedPrefsFileName;
		sdCardDBDirectoryPath = sdPath + "/MyWallet/databases/";
		sdCardSPPath = sdPath + "/MyWallet/shared_prefs/" + sharedPrefsFileName;
	}
	
	@SuppressWarnings("resource")
	public boolean exportToSdCard(int noOfBackup) {
		boolean isSuccess = false;
		try {
			if (new File(sdPath).canWrite()) {
				loadFileList();
				Log.i("exportToSdCard", "We can write in sdCard.");
				File currentDB = new File(dataDBPath);
				File backupDBDirectory = new File(sdCardDBDirectoryPath);
				if (!backupDBDirectory.exists()) {
					backupDBDirectory.mkdirs();
				}
				File backupDB = new File(backupDBDirectory, Constants.DB_NAME + "_" + System.currentTimeMillis());
				if (currentDB.exists() && backupDB.createNewFile()) {
					long bytesTransferred = 0;
					FileChannel sourceChannel = new FileInputStream(currentDB).getChannel();
					FileChannel destinationChannel = new FileOutputStream(backupDB).getChannel();
					bytesTransferred = destinationChannel.transferFrom(sourceChannel, 0, sourceChannel.size());
					sourceChannel.close();
					destinationChannel.close();
					int i = mFileList.length - noOfBackup;
					while (i >= 0) {
						File file = new File(backupDBDirectory, mFileList[i--]);
						file.delete();
					}
					isSuccess = bytesTransferred > 0;
				} else {
					isSuccess = false;
				}
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return isSuccess;
	}
	
	@SuppressWarnings("resource")
	public void exportPreferencesTosdCard() {
		File currentSP = new File(dataSPPath);
		File backupSP = new File(sdCardSPPath);
		if (!backupSP.getParentFile().exists()) {
			backupSP.getParentFile().mkdirs();
		}
		if (backupSP.exists()) {
			backupSP.delete();
		}
		try {
			if (currentSP.exists() && backupSP.createNewFile()) {
				FileChannel sourceChannel = new FileInputStream(currentSP).getChannel();
				FileChannel destinationChannel = new FileOutputStream(backupSP).getChannel();
				destinationChannel.transferFrom(sourceChannel, 0, sourceChannel.size());
				sourceChannel.close();
				destinationChannel.close();
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	@SuppressWarnings("resource")
	public boolean importFromSdCard(String fileName) {
		boolean isSuccess = false;
		try {
			if (new File(sdPath).canWrite()) {
				File dataDB = new File(dataDBPath);
				File sdCardDBDirectory = new File(sdCardDBDirectoryPath);
				if (!dataDB.getParentFile().exists()) {
					dataDB.getParentFile().mkdirs();
				}
				File sdCardDB = new File(sdCardDBDirectory, fileName);
				if (sdCardDB.exists()) {
					long bytesTransferred = 0;
					FileChannel sourceChannel = new FileInputStream(sdCardDB).getChannel();
					FileChannel destinationChannel = new FileOutputStream(dataDB).getChannel();
					bytesTransferred = destinationChannel.transferFrom(sourceChannel, 0, sourceChannel.size());
					sourceChannel.close();
					destinationChannel.close();
					isSuccess = bytesTransferred > 0;
				} else {
					isSuccess = false;
				}
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return isSuccess;
	}
	
	public void importPreferencesTosdCard() {
		File dataSP = new File(dataSPPath);
		File sdCardSP = new File(sdCardSPPath);
		if (dataSP.canWrite()) {
			if (dataSP.exists()) {
				dataSP.delete();
			}
			try {
				if (sdCardSP.exists()) {
					FileChannel sourceChannel = new FileInputStream(sdCardSP).getChannel();
					FileChannel destinationChannel = new FileOutputStream(dataSP).getChannel();
					long bt = destinationChannel.transferFrom(sourceChannel, 0, sourceChannel.size());
					Log.i("MyWallet", "Source Folder:" + sdCardSP + ", Destination Folder: " + dataSP
							+ ", Byte Tranferred: " + bt);
					sourceChannel.close();
					destinationChannel.close();
				}
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	private void loadFileList() {
		File sdCardDBDirectory = new File(sdCardDBDirectoryPath);
		if (sdCardDBDirectory.exists()) {
			FilenameFilter filter = new FilenameFilter() {
				@Override
				public boolean accept(File dir, String filename) {
					return filename.contains(FTYPE);
				}
			};
			mFileList = sdCardDBDirectory.list(filter);
		} else {
			sdCardDBDirectory.mkdirs();
			mFileList = new String[0];
			return;
		}
		Arrays.sort(mFileList);
	}
}
