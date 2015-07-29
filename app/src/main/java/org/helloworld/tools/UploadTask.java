package org.helloworld.tools;

import android.util.Base64;

import org.ksoap2.serialization.SoapObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

/**
 * 同步方式分块上传文件
 */
public class UploadTask
{
	private String remoteFolder;
	private String remoteName;
	/**
	 * 以Byte为单位
	 */
	private long blockSize;
	byte[] bytes;
	int blockNum;

	/**
	 * @param blockSize 块大小,单位byte
	 * @param localFile 本地文件全路径
	 * @param remoteFolder 远程文件夹路径
	 * @param remoteName 远程文件夹名称
	 * */
	public UploadTask(long blockSize, String localFile, String remoteName, String remoteFolder) throws IOException
	{
		this.blockSize = blockSize;
		this.remoteName = remoteName;
		this.remoteFolder = remoteFolder;
		File f = new File(localFile);
		blockNum = (int) (f.length() / blockSize) + 1;
		FileInputStream fin;

		fin = new FileInputStream(f);
		ByteArrayOutputStream out = new ByteArrayOutputStream(1000);
		byte[] b = new byte[1000];
		int n;

		while ((n = fin.read(b)) != -1)
			out.write(b, 0, n);

		fin.close();
		out.close();
		bytes = out.toByteArray();
	}
	/**
	 * @param blockSize 块大小,单位byte
	 * @param remoteFolder 远程文件夹路径
	 * @param remoteName 远程文件夹名称
	 * @param bytes 以字节数组表示的整个文件内容
	 * */
	public UploadTask(String remoteFolder, String remoteName, long blockSize, byte[] bytes)
	{
		this.blockNum = ((int) (bytes.length / blockSize))+1;
		this.bytes = bytes;
		this.remoteName = remoteName;
		this.remoteFolder = remoteFolder;
		this.blockSize = blockSize;
	}

	public Boolean call()
	{
		try
		{
			for (int i = 0; i < blockNum; i++)
			{
				WebService upload = new WebService("uploadFile");
				String base64;
				if ((i + 1) * blockSize < bytes.length)
					base64 = Base64.encodeToString(bytes, ((int) (i * blockSize)), (int) blockSize, Base64.DEFAULT);
				else
					base64 = Base64.encodeToString(bytes, ((int) (i * blockSize)), (int) (bytes.length - i * blockSize), Base64.DEFAULT);
				upload.addProperty("path", remoteFolder).addProperty("fileName", remoteName).addProperty("base64", base64).addProperty("blockSerial", i);

				SoapObject soapObject = upload.call();
				if (!Boolean.parseBoolean(soapObject.getPropertyAsString(0))) return false;
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
			return false;
		}
		return true;
	}


}
