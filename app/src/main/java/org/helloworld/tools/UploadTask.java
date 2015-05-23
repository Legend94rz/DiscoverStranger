package org.helloworld.tools;

import android.util.Base64;

import org.ksoap2.serialization.SoapObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

/**
 * Created by Administrator on 2015/5/20.
 * 同步方式分块上传文件
 */
public class UploadTask
{
	private String localFile;
	private String remotePath;
	private String remoteName;
	/**
	 * 以Byte为单位
	 */
	private long blockSize;
	byte[] bytes;
	int blockNum;


	public UploadTask(long blockSize, String localFile, String remoteName, String remotePath) throws IOException
	{
		this.blockSize = blockSize;
		this.localFile = localFile;
		this.remoteName = remoteName;
		this.remotePath = remotePath;
		File f = new File(localFile);
		blockNum = (int) (f.length() / blockSize) + 1;
		FileInputStream fin = null;

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

	public UploadTask(String remotePath, String remoteName, long blockSize, byte[] bytes)
	{
		this.blockNum = ((int) (bytes.length / blockSize))+1;
		this.bytes = bytes;
		this.remoteName = remoteName;
		this.remotePath = remotePath;
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
				upload.addProperty("path", remotePath).addProperty("fileName", remoteName).addProperty("base64", base64).addProperty("blockSerial", i);

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
