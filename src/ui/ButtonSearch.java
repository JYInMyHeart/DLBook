package ui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Date;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static Config.config.*;

import javax.swing.JButton;
import core.DLBook;

@SuppressWarnings("serial")
public class ButtonSearch extends JButton{
	private PanelControl pc = null;
	private String lastkey = null;
	
	private class searchbook implements Callable<DLBook>
	{
		private String website;
		private String key;
		
		public searchbook(String website, String key)
		{
			this.website = website;
			this.key = key;
		}
		
		@Override
		public DLBook call() throws Exception {
			DLBook dlbook = null;
			try {
				Class<?> cls = Class.forName(website);
				Constructor<?> con = cls.getConstructor(String.class,PanelControl.class, int.class);
				int poolsize = config.getWebsites().get(website);
				dlbook = (DLBook) con.newInstance(key, pc, poolsize);
			} catch (Exception e1) {
				e1.printStackTrace();
				System.out.println("类操作失败，类名"+website);
			}
			
			return dlbook;
		}		
	}
	
	private class MouseClick implements ActionListener
	{
		@Override
		public void actionPerformed(ActionEvent e) {
			if(!isEnabled()) return;
			
			String key = pc.getKey().trim();
			if(key.equals(lastkey) || key.length() == 0)
			{
				pc.setStateMsg("两次搜索结果一致或关键字为空，不进行搜索",false);
				return;
			}
			lastkey = key;
			pc.emptySearchRst();
			pc.setStateMsg(String.format("%tT:开始搜索%s", new Date(), key),true);
			setEnabled(false);
			setText("搜索中...");
			paintImmediately(0, 0, getWidth(), getHeight());
			
			ArrayList<Future<DLBook>> futures = new ArrayList<Future<DLBook>>();
			ExecutorService pool = Executors.newCachedThreadPool();
			for(String website : config.getWebsites().keySet())
			{
				futures.add(pool.submit(new searchbook(website, key)));
			}
			pool.shutdown();
			
			for(Future<DLBook> future :  futures)
			{
				DLBook dlbook = null;
				try {
					dlbook = future.get();
				} catch (InterruptedException | ExecutionException e1) {
					e1.printStackTrace();
					System.out.println("搜索多线程被打断");
				}
				if(dlbook == null) continue;
				
				pc.addBookinfos(dlbook);
			}
			
			pc.flashtablelist();
			setEnabled(true);
			setText("搜索");
		}
	}
	
	public ButtonSearch(PanelControl pc)
	{
		this.pc = pc;
		setText("搜索");
		setBounds(config.getFramew() - 150 - 15, 5, 75, 25);
		this.addActionListener(new MouseClick());
	}
}
