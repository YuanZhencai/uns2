package com.wcs.uns.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * <p>Project: uns</p>
 * <p>Description: </p>
 * <p>Copyright (c) 2011 Wilmar Consultancy Services</p>
 * <p>All Rights Reserved.</p>
 * @author <a href="mailto:shenbo@wcs-global.com">Shen Bo</a>
 */
public class UnsUtil {

	private URL url = null;
	Logger logger = Logger.getLogger("com.wcs.uns");
	
	/**
	 * 通过HTTP GET方式获得资源信息
	 * @param type
	 * @param subpath
	 * @param queryMap
	 * @return
	 * @throws IOException
	 */
	public String getResource(String type, String subpath, Map<String, String> queryMap) {
		
		String resource = null;
		StringBuffer resources = new StringBuffer();
		
		// type:资源根; subpath:@PathParam; queryMap:@QueryParam
		URL u = urlBulider(type, subpath, queryMap);
		
		//make connection
		HttpURLConnection urlc = null;
		BufferedReader br = null;
		try {
			urlc = (HttpURLConnection)u.openConnection();
			
			//use get mode
			urlc.setDoInput(true);
			urlc.setDoOutput(false);
			urlc.setAllowUserInteraction(false);

			//get result
			br = new BufferedReader(new InputStreamReader(urlc.getInputStream(),"UTF8"));
			
			while ((resource=br.readLine())!=null) {
				resources.append(resource);
			}
			
		} catch (IOException e) {
			// do nothing
		} finally {
			if (br != null) {
				try {
					br.close();
				} catch (IOException e) {
					// do nothing
				}
			}
			if (urlc != null) {
				urlc.disconnect();
			}
			
		}

		return resources.toString();
	}
	
	/**
	 * 构建URL
	 * @param path
	 * @param subpath
	 * @param queryMap
	 * @return
	 */
	public URL urlBulider(String path, String subpath, Map<String, String> queryMap) {
		// /users/subpath/?query1=q1&query2=q2
		StringBuffer sbf = new StringBuffer();
		sbf.append(UnsConsts.MARK_SLASH).append(path);
		sbf.append(UnsConsts.MARK_SLASH).append(subpath);
		sbf.append(UnsConsts.MARK_QUE);
		
		try {
			for (String key:queryMap.keySet())
				{ sbf.append(key + UnsConsts.MARK_EUQ + URLEncoder.encode(queryMap.get(key)==null?"":queryMap.get(key), UnsConsts.UTF8)).append(UnsConsts.MARK_AMPERSAND); }
			// delete the last "&"
			sbf.deleteCharAt(sbf.length() - 1);
			url = new URL(UnsConsts.BASEPATH + sbf.toString());
		} catch (UnsupportedEncodingException ex) {
			ex.printStackTrace();
		} catch (MalformedURLException e) {
			logger.log(Level.SEVERE, "A malformed URL has occurred!url={0}",url.getPath());
		}
		return url;
	}
	
	/**
	 * Check if string is null or empty
	 * @param str
	 * @return
	 */
	public static boolean isNullOrEmpty(String str) {
		if (str == null) {
			return true;
		} else if (UnsConsts.MARK_EMPTY.equals(str) || (str.trim()).length() == 0) {
			return true;
		}
		return false;
	}
	
}
