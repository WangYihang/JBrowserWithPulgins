package plugins.downloadLinkAnalyzer;

/**
 * Created by Sun on 2016/11/30.
 */
import org.json.JSONException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * 主界面
 * Created by 王一航 on 2016/7/23.
 */
public class DownloadLinkAnalyzerFrame extends JFrame{
    //全局变量
    static String url;
    static java.util.List<String> resultDownloadLionks;
    static String softwareName;

    //声明控件
    JTextField jTextField_url;
    JButton jButton_start;
    JPanel jPanel_title;

    //需要在外部更新日志
    //TODO 考虑这种形式的合理性
    static JTextArea jTextArea_result;
    JScrollPane jScrollPane_result;
    JPanel jPanel_result;
    /**
     * 构造方法
     */
    public DownloadLinkAnalyzerFrame(String url){
        this.url = url;
        initView();
        setView(url);
        addView();
        initEvent();
    }

    /**
     * 实例化控件
     */
    private void initView() {
        jTextField_url = new JTextField("请输入URL");
        jButton_start = new JButton("开始分析");
        jPanel_title = new JPanel();
        jTextArea_result = new JTextArea();
        jScrollPane_result = new JScrollPane(jTextArea_result);
        jPanel_result = new JPanel();

    }

    /**
     * 设置控件属性
     */
    private void setView(String url) {
        jTextField_url.setText(url);
        jTextArea_result.setText("1.在上方文本框中输入下载详情页的URL\n" +
                "(例如:http://www.orsoon.com/Soft/14553.html)\n" +
                "2.点击\"开始分析\"按钮\n" +
                "3.稍等1-2秒钟,在下方会解析出正确的下载链接\n");
        jPanel_title.setLayout(new BorderLayout());
        jPanel_result.setLayout(new BorderLayout());
        this.setTitle("直接找到下载地址  By:王一航");
        this.setLayout(new BorderLayout());
        this.setSize(400,400);
        this.setVisible(true);
        this.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
    }

    /**
     * 添加控件从属关系
     */
    private void addView() {
        jPanel_title.add(jTextField_url,BorderLayout.CENTER);
        jPanel_title.add(jButton_start,BorderLayout.EAST);
        jPanel_result.add(jScrollPane_result);
        this.add(jPanel_title,BorderLayout.NORTH);
        this.add(jPanel_result,BorderLayout.CENTER);
    }

    /**
     * 添加监听事件
     * 调度程序(核心)
     */
    private void initEvent() {
        jButton_start.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                //打印分隔符
                jTextArea_result.append("**********************************************\n");
                //TODO 业务逻辑
                String url = jTextField_url.getText();
                String module = "";//解析规则模式
                String content = "";//待解析的解析网页内容
                module = DownloadLinkAnalyzerUtils.URLParser(url);
                url = DownloadLinkAnalyzerUtils.URLHandler(module,url);
                if (!(module.equals(""))){//存在解析模块可以解析
                    //日志
                    jTextArea_result.append("存在解析模块 : " + module + ", 可以解析\n");
                    content = DownloadLinkAnalyzerUtils.getContentAsString(url, module);
                    Document document = Jsoup.parse(content);
                    softwareName = DownloadLinkAnalyzerUtils.getSoftwareName(module, document);
                    try {
                        resultDownloadLionks = DownloadLinkAnalyzerUtils.getDownloadLinks(module,document);
                    } catch (JSONException e1) {
                        e1.printStackTrace();
                    }
                    //日志
                    jTextArea_result.append("页面名称为(软件名称) : \n" + softwareName + "\n");
                    for (String res: resultDownloadLionks
                            ) {
                        //日志
                        jTextArea_result.append("下载地址 : \n" + res + "\n");
                    }
                }else{
                    //TODO 给用户提示暂不支持此网站
                    //日志
                    jTextArea_result.append("暂不支持该网站!\n");
                }
            }
        });
    }
}
