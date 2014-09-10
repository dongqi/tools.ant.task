package cn.eastseven.tools.ant.task;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.Vector;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.DirectoryScanner;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.types.FileSet;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpException;

public class SFTPTask extends Task {

	private String msg;

	private String username;
	private String password;
	private String host;
	private String destDir;

	private int port;

	private Vector<FileSet> filesets = new Vector<FileSet>();

	@Override
	public void execute() throws BuildException {
		System.out.println(msg);

		try {
			sftp();
		} catch (JSchException e) {
			e.printStackTrace();
		}

	}

	private void sftp() throws JSchException {
		JSch jsch = new JSch();

		Session session = jsch.getSession(username, host, port);

		session.setConfig("StrictHostKeyChecking", "no");
		session.setPassword(password);
		session.setTimeout(30000);
		session.connect();
		
		Channel channel = (Channel) session.openChannel("sftp");
		channel.connect(1000);
		ChannelSftp sftp = (ChannelSftp) channel;
		
		Set<String> includedFiles = new HashSet<String>();
		String dir = "";
	    for (Iterator<FileSet> iterator = filesets.iterator(); iterator.hasNext();) {
	        FileSet fs = iterator.next();
	        DirectoryScanner ds = fs.getDirectoryScanner(getProject());
	        String[] _includedFiles = ds.getIncludedFiles();
	        dir = fs.getDir().getAbsolutePath();
	        for(String filename : _includedFiles) {
	        	includedFiles.add(dir+"/"+filename);
	        }
	    }

	    for (String absolutePath : includedFiles) {
			String filename = absolutePath.substring(absolutePath.lastIndexOf("/"));
			
			String src = absolutePath;
			String dst = destDir+"/"+filename;
			
			System.out.println("上传开始: src=" + absolutePath+", dst="+dst);
			try {
				sftp.put(src, dst, ChannelSftp.OVERWRITE);
			} catch (SftpException e) {
				e.printStackTrace();
			}
			
			System.out.println("上传完成");
		}
	    
	    channel.disconnect();
		session.disconnect();
		System.out.println("关闭上传连接");
	    
	}

	// ----- setter method

	public void setMessage(String msg) {
		this.msg = msg;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public void setDestDir(String destDir) {
		this.destDir = destDir;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public void addFileset(FileSet fileset) {
		filesets.add(fileset);
	}
}
