package plugins.downloadLinkAnalyzer;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;

/**
 * 工具类:
 * Created by 王一航 on 2016/7/23.
 */
public class DownloadLinkAnalyzerUtils {
    /**
     * URL解析器(解析URL并分发给对应的模块进行处理)
     * @param url
     * @return
     */
    public static String URLParser(String url){
        String modules = "";
        if (url.contains("www.jz5u.com")){//jz5u绿色下载
            modules = "jz5u";
        }else if (url.contains("www.orsoon.com")){//未来软件园
            modules = "orsoon";
        }else if (url.contains("www.crsky.com")){//非凡软件站
            modules = "crsky";
        }else if (url.contains("www.onlinedown.net")){//华军软件园
            modules = "onlinedown";
        }else if (url.contains("www.xiazaiba.com")){//下载吧
            modules = "xiazaiba";
        }else if (url.contains("www.greenxiazai.com")){//绿色下载站
            modules = "greenxiazai";
        }else if (url.contains("www.downg.com")){//绿软家园
            modules = "downg";
        }else if (url.contains("dl.pconline.com.cn")){//太平洋电脑网
            modules = "pconline";
        }else if (url.contains("www.duote.com")){//多特软件站
            modules = "duote";
        }else if(url.contains("www.3987.com")){//统一下载站
            modules = "3987";
        }else {
            modules = "";
        }
        return modules;
    }


    /**
     * 获取给定URL网页内容
     * @param url
     * @return
     */
    public static String getContentAsString(String url, String modules){
        //保存结果
        String temp = "";
        String content = "";
        try {
            //判断URL是否正确
            URL myUrl = new URL(url);
            URLConnection urlConnection = myUrl.openConnection();
            InputStream inputStream = urlConnection.getInputStream();
            InputStreamReader inputStreamReader = null;
            //TODO 解决不同网站编码不同造成的乱码问题
            switch (modules){
                case "jz5u":
                case "xiazaiba":
                case "crsky":
                case "greenxiazai":
                case "downg":
                case "pconline":
                case "duote":
                    inputStreamReader = new InputStreamReader(inputStream,"GBK");
                    break;
                case "orsoon":
                case "onlinedown":
                case "3987":
                    inputStreamReader = new InputStreamReader(inputStream,"UTF-8");
                    break;
                default:
                    //TODO 因为之前已经对modules进行了筛选,理论上来说,是不会进入Default的
                    break;
            }
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
            while((temp =bufferedReader.readLine()) != null){
                content = content + temp;
            }
            bufferedReader.close();
            inputStreamReader.close();
            inputStream.close();
        } catch (MalformedURLException e) {
            System.out.println("输入URL非法!请检查是否以\"http://\"开头");
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        //返回
        return content;
    }

    /**
     * 获得页面中所有的下载链接
     * @param document
     * @param modules
     * @return 下载地址集合
     */
    public static List<String> getDownloadLinks(String modules, Document document) throws JSONException {
        List<String> downloadLinks = new ArrayList<>();
        switch (modules){
            case "jz5u"://jz5u绿色下载
                //选出真正DIV
                Elements element_div = document.getElementsByClass("co_content5");
                if (element_div.isEmpty()){//没有获取到下载地址
                    //TODO 将来给用户提示
                    System.out.println("输入网址有误,请您检查是否输入地址是否是软件的详情页");
                }else{//成功解析到下载地址
                    Element element = element_div.get(0);
                    Elements a = element.getElementsByTag("a");
                    for (Element e: a) {
                        if (!(e.text().contains("高速")) && (e.text().contains("本地"))){
                            downloadLinks.add(e.attr("href"));
                        }
                    }
                }
                break;
            case "orsoon"://未来软件园
                Element element_pc = document.getElementById("x_downfile");
                String content = element_pc.toString();
                int start_orsoon = content.indexOf("push('");
                int end = content.indexOf("');");
                String result = content.substring(start_orsoon + 6, end);
                //TODO 完成安卓客户端/苹果客户端
                downloadLinks.add(result);
                break;
            case "crsky"://非凡软件园
                Elements elements_crsky = document.getElementsByAttribute("itemprop");
                for (Element e: elements_crsky
                        ) {
                    if (e.attr("itemprop").equals("downloadUrl")){
                        downloadLinks.add(e.attr("href"));
                    }
                }
                break;
            case "onlinedown"://华军软件园
                //刚好这个script标签在下载链接之前
                Elements element_js_before = document.getElementsByAttributeValue("src", "http://d.onlinedown.net/php/ajax_ip_1.2.php");
                Element onlinedown_true = element_js_before.get(0).nextElementSibling();
                String onlinedown_true_links = onlinedown_true.toString();
                int json_onlinedown_start = onlinedown_true_links.indexOf("var durl = ");
                int json_onlinedown_end = onlinedown_true_links.indexOf("]");
                String json_onlinedown = onlinedown_true_links.substring(json_onlinedown_start + 11,json_onlinedown_end + 1);
                //解析json对象
                JSONArray jsonArray = new JSONArray(json_onlinedown);
                for(int i = 0; i < jsonArray.length(); i++){
                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                    //TODO 分开下载链接和服务器地址信息
                    //TODO 加入javabean
                    downloadLinks.add(jsonObject.getString("url") + " 服务器地址 : " + jsonObject.getString("name"));
                }
                break;
            case "xiazaiba":
                String content_xiazaiba = document.toString();
                int start_xiazaiba = content_xiazaiba.indexOf("downlist(");
                int end_xiazaiba = content_xiazaiba.indexOf("','");
                String temp_xiazaiba = content_xiazaiba.substring(start_xiazaiba, end_xiazaiba);
                int temp_start_xiazaiba = temp_xiazaiba.indexOf("\",TypeID:\"");
                int temp_end_xiazaiba = temp_xiazaiba.length();
                String half_xiazaiba = temp_xiazaiba.substring(temp_start_xiazaiba + 2, temp_end_xiazaiba);
                String full_xiazaiba = "http://xiazai.xiazaiba.com" + half_xiazaiba;
                downloadLinks.add(full_xiazaiba);
                break;
            case "greenxiazai":
                Elements elements_greenxiazai = document.getElementsByAttributeValue("onclick", "SetHome();");
                for (Element e: elements_greenxiazai
                     ) {
                    downloadLinks.add(e.attr("href"));
                }
                break;
            case "downg":
                Elements elements_downg = document.getElementsByClass("download-list");
                Elements elements_downg_a = elements_downg.get(0).getElementsByTag("a");
                for (Element e: elements_downg_a
                     ) {
                    downloadLinks.add(e.attr("href"));
                }
                break;
            case "pconline":
                Elements elements_pconline = document.getElementsByAttribute("tempUrl");
                for (Element e:elements_pconline
                     ) {
                    if (e.attr("class").equals("link-a")){//屏蔽高速下载,只留下本地下载
                        downloadLinks.add(e.attr("tempUrl"));
                    }
                }
                break;
            case "duote":
                //TODO 添加对该网站手机模块的支持(暂时只支持PC软件网页处理)
                String content_duote = document.toString();
                int start_duote = content_duote.indexOf("var sPubdown = '");
                int ent_duote = content_duote.indexOf("var serUrl = '';");
                String half_duote = content_duote.substring(start_duote + 16, ent_duote - 2);
                downloadLinks.add(half_duote);
                break;
            case "3987":
                Elements elements_3987 = document.getElementsByClass("dl-ico");
                for (Element e: elements_3987
                     ) {
                    Element element_3987 = e.nextElementSibling();
                    downloadLinks.add(element_3987.attr("href"));
                }
                break;
            default:
                break;
        }
        return downloadLinks;
    }

    /**
     * 获取软件名称
     * @param modules
     * @param document
     * @return
     */
    public static String getSoftwareName(String modules,Document document) {
        String softwareName = "";
        Elements title = document.getElementsByTag("title");
        Element tile_element = title.get(0);
        String title_string = tile_element.text();;
        switch (modules){
            case "jz5u":
                if (title_string.contains("-")){
                    softwareName = title_string.split("-")[0];
                }
                break;
            case "orsoon":
                if (title_string.contains(" - ")){
                    softwareName = title_string.split(" - ")[0];
                }
                break;
            case "crsky":
                if (title_string.contains("下载_")){
                    softwareName = title_string.split("下载_")[0];
                }
                break;
            case "onlinedown":
                if (title_string.contains(" - ")){
                    softwareName = title_string.split(" - ")[0];
                }
                break;
            case "xiazaiba":
            case "greenxiazai":
                String temp1_greenxiazai = title_string;
                String temp2_greenxiazai = title_string;
                if (title_string.contains("-")){
                    temp1_greenxiazai = title_string.split("-")[0];
                }
                if (title_string.contains("\\|")){
                    temp2_greenxiazai = temp1_greenxiazai.split("\\|")[1];//注 : '|'的转义字符是'\\|'
                }
                softwareName = temp2_greenxiazai;
                break;
            case "downg":
                //TODO 抽取软件名
                softwareName = title_string;
                break;
            case "pconline":
                String temp1_pconline = title_string;
                String temp2_pconline = title_string;
                if (title_string.contains("\\【")){
                    temp1_pconline = title_string.split("\\【")[0];
                }
                if (title_string.contains("_")){
                    temp2_pconline = temp1_pconline.split("_")[1];
                }
                softwareName = temp2_pconline;
                break;
            case "duote":
                String temp1_duote = title_string;
                String temp2_duote = title_string;
                if (title_string.contains("\\】")){
                    temp1_duote = title_string.split("\\】")[1];
                }
                if (title_string.contains("_")){
                    temp2_duote = temp1_duote.split("_")[0];
                }
                softwareName = temp2_duote;
                break;
            case "3987":
                String temp1_3987 = title_string;
                String temp2_3987 = title_string;
                if (title_string.contains("_")){
                    temp1_3987 = title_string.split("_")[0];
                }
                if (title_string.contains("\\|")){
                    temp2_3987 = temp1_3987.split("\\|")[1];//注 : '|'的转义字符是'\\|'
                }
                softwareName = temp2_3987;
                break;
            default:
                softwareName = title_string;
                break;
        }
        return softwareName;
    }

    /**
     * URL处理(有的时候用户需要多次跳转才能到达真正的下载页面)
     * 功能: 直接跳转到真正页面
     * @return
     */
    public static String URLHandler(String module, String url) {
        switch (module){
            case "jz5u":
                if (url.contains("html")){//判断是否是真正的下载页面(如果包含了html,有可能需要再跳转一下)
                    String fileName = url.split("/")[url.split("/").length - 1];
                    int indexOfPoint = fileName.indexOf(".");
                    String fileNumber = fileName.substring(0, indexOfPoint);
                    url = "http://www.jz5u.com/Soft/softdown.asp?softid=" + fileNumber;
                }
                break;
            case "onlinedown":
                if (url.contains("htm") && (!url.contains("_"))) {//判断是否是真正的下载页面(如果包含了htm,有可能需要再跳转一下)
                    String fileName = url.split("/")[url.split("/").length - 1];
                    int indexOfPoint = fileName.indexOf(".");
                    String fileNumber = fileName.substring(0, indexOfPoint);
                    url = "http://www.onlinedown.net/softdown/" + fileNumber + "_2.htm";
                }
                break;
            case "pconline":
                if (url.contains("html") && (!url.contains("-"))) {
                    String filename = url.split("/")[url.split("/").length - 1];
                    String fileNumber = filename.split("\\.")[0];
                    url = "http://dl.pconline.com.cn/download/" + fileNumber + "-1.html";
                }
                break;
            default:
                break;
        }
        return url;
    }

    /**
     * Unicode转中文
     */
    public void unicodeToChinese(){
        //TODO 完成编码转换(华军软件园)

    }


    /**
     * base64加密
     * @param content 明文
     * @return 密文
     */
    public static String myBASE64Encoder(String content){
        String result;
        BASE64Encoder base64Encoder = new BASE64Encoder();
        result = base64Encoder.encodeBuffer(content.getBytes());
        return result;
    }


    /**
     * base64解密
     * @param content 明文
     * @return 密文
     */
    public static String myBASE64Decoder(String content){
        String result = "";
        BASE64Decoder base64Decoder = new BASE64Decoder();
        try {
            byte[] bytes = base64Decoder.decodeBuffer(content);
            result = new String(bytes, "UTF-8");
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }
}
