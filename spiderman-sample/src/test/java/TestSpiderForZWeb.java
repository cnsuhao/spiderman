//
//import java.util.Date;
//import java.util.List;
//import java.util.Map;
//
//import model.Category;
//import model.Log;
//import model.Match;
//import model.Team;
//import net.zweb.core.SimpleZWeb;
//import net.zweb.core.ZWeb;
//import net.zweb.core.config.Config;
//import net.zweb.core.config.MapConfig;
//import net.zweb.core.db.Db;
//
//import org.eweb4j.config.EWeb4JConfig;
//import org.eweb4j.spiderman.fetcher.FetchResult;
//import org.eweb4j.spiderman.spider.SpiderListener;
//import org.eweb4j.spiderman.spider.SpiderListenerAdaptor;
//import org.eweb4j.spiderman.spider.Spiderman;
//import org.eweb4j.spiderman.task.Task;
//import org.eweb4j.util.CommonUtil;
//import org.junit.Test;
//
//import com.alibaba.fastjson.JSON;
//
//public class TestSpiderForZWeb {
//	
//    public static void main(String[] args) {
//        String h = CommonUtil.getNowTime("HH");
//        System.out.println(h);
//    }
//    
//	@Test
//	public void test() throws Exception {
//		
//		String err = EWeb4JConfig.start();
//		if (err != null)
//			throw new Exception(err);
//		
//		//实例化ZWeb
//		ZWeb zweb = new SimpleZWeb();
//		zweb.startup();
//		final Db db = zweb.getDb();
//		
//		SpiderListener listener = new SpiderListenerAdaptor(){
//			public void onAfterScheduleCancel(){
//				//调度结束回调
//			}
//			/**
//			 * 每次调度执行前回调此方法
//			 * @param theLastTimeScheduledAt 上一次调度时间
//			 */
//			public void onBeforeEveryScheduleExecute(Date theLastTimeScheduledAt){
//				System.err.print("[SPIDERMAN] "+CommonUtil.getNowTime("HH:mm:ss")+" [LAST_SCHEDULE_AT] ~ ");
//				System.err.println("at -> " + CommonUtil.formatTime(theLastTimeScheduledAt));
//			}
//			
//			public void onFetch(Thread thread, Task task, FetchResult result) {
//				System.out.print("[SPIDERMAN] "+CommonUtil.getNowTime("HH:mm:ss")+" [FETCH] ~ ");
//				System.out.println("fetch result ->" + result + " from -> " + task.sourceUrl);
//			}
//			
//			public void onInfo(Thread thread, Task task, String info) {
//				System.out.print("[SPIDERMAN] "+CommonUtil.getNowTime("HH:mm:ss")+" [INFO] ~ ");
//				System.out.println(info);
//			}
//			
//			public void onError(Thread thread, Task task, String err, Throwable e) {
//				System.err.print("[SPIDERMAN] "+CommonUtil.getNowTime("HH:mm:ss")+" [ERROR] ~ ");
//				System.err.print(err);
//				e.printStackTrace();
//			}
//			
//			public void onParseOne(Thread thread, Task task, int size, int index, Map<String, Object> model) {
//			    String target = task.target.getName();
//			    System.out.println("============= TARGET:"+target+"================");
//			    
//				//将数据入库
//			        System.out.println(model);
//			        if (true) return;
//			        Config<String, Object> data = new MapConfig<String, Object>(model);
//			        String categoryName = data.getString("category");
//			        String homeTeamName = data.getString("home_team");
//			        String visitingTeamName = data.getString("visiting_team");
//			        String time = data.getString("time");
//			        
//			        if (CommonUtil.isBlank(categoryName)) return;
//			        if (CommonUtil.isBlank(homeTeamName)) return;
//			        if (CommonUtil.isBlank(visitingTeamName)) return;
//			        if (CommonUtil.isBlank(time)) return;
//			        
//			        //1. handle category
//			        Category category = db.selectOneByWhere(Category.class, "name = ?", categoryName);
//			        if (category == null) {
//			            category = new Category();
//			            category.setName(categoryName);
//			            db.create(category);
//			        }
//			        
//			        //2. handle team
//			        Team homeTeam = db.selectOneByWhere(Team.class, "name = ?", homeTeamName);
//			        if (homeTeam == null) {
//			            homeTeam = new Team();
//			            homeTeam.setName(homeTeamName);
//			            db.create(homeTeam);
//			        }
//			        Team visitingTeam = db.selectOneByWhere(Team.class, "name = ?", visitingTeamName);
//			        if (visitingTeam == null) {
//			            visitingTeam = new Team();
//			            visitingTeam.setName(visitingTeamName);
//			            db.create(visitingTeam);
//			        }
//			        
//			        String filter = "category_id = ? and home_team_id = ? and visiting_team_id = ?";
//			        Match match = db.selectOneByWhere(Match.class, filter, category.getId(), homeTeam.getId(), visitingTeam.getId());
//			        if (match == null) {
//			            match = new Match();
//			        }
//			        
//			        match.setCategory(category);
//			        match.setHomeTeam(homeTeam);
//			        match.setVisitingTeam(visitingTeam);
//			        
//			        StringBuilder updateLog = new StringBuilder();
//			        
//			        // 处理时间, match 和 history 处理策略不同
//                    Date targetTime = null;
//                    if ("live".equals(task.target.getName())) {
//                        String[] ts = time.split(":");
//                        int h = CommonUtil.toInt(ts[0]);
//                        
//                        //当日11:00开始到次日11:00
//                        Date date = CommonUtil.parse("yyyy-MM-dd", CommonUtil.getNowTime("yyyy-MM-dd"));
//                        if (h < 11) {
//                            //算第二天
//                            date = CommonUtil.addDay(date, 1);
//                        }
//                        
//                        String dateTimeStr = CommonUtil.formatTime("yyyy-MM-dd", date) + " " + time;
//                        targetTime = CommonUtil.parse("yyyy-MM-dd HH:mm", dateTimeStr);
//                    } else if ("history".equals(task.target.getName())) {
//                        //06-12 13:00
//                        String dateTimeStr = CommonUtil.getNowTime("yyyy-") + time;
//                        targetTime = CommonUtil.parse("yyyy-MM-dd HH:mm", dateTimeStr);
//                    }
//			        if (match.getTime() != null) {
//                        String oldStr = CommonUtil.formatTime("yyyy-MM-dd HH:mm", match.getTime());
//                        String targetTimeStr = CommonUtil.formatTime("yyyy-MM-dd HH:mm", targetTime);
//                        if (!oldStr.equals(targetTimeStr)) {
//                            updateLog.append("time["+oldStr+" -> "+targetTimeStr+"]");
//                        }
//                    }
//			        match.setTime(targetTime);
//			        
//			        Integer homeScore = data.getInt("home_score", 0);
//			        if (homeScore > 0 && !homeScore.equals(match.getHomeScore())) {
//			            updateLog.append("homeScore["+match.getHomeScore()+" -> "+homeScore+"]");
//			            match.setHomeScore(homeScore);
//			        }
//			        Integer visitingScore = data.getInt("visiting_score", 0);
//                    if (visitingScore > 0 && !visitingScore.equals(match.getVisitingScore())) {
//                        updateLog.append("visitingScore["+match.getVisitingScore()+" -> "+visitingScore+"]");
//                        match.setVisitingScore(visitingScore);
//                    }
//			        String halfScore = data.getString("half_score");
//			        if (!CommonUtil.isBlank(halfScore) && !halfScore.equals(match.getHalfScore())) {
//                        updateLog.append(",halfScore["+match.getHalfScore()+" -> "+halfScore+"]");
//                        match.setHalfScore(halfScore);
//                    }
//			        
//			        String firstLineDesc = data.getString("first_line");
//			        if (!CommonUtil.isBlank(firstLineDesc) && !firstLineDesc.equals(match.getFirstLine())) {
//                        updateLog.append(",firstLine["+match.getFirstLine()+" -> "+firstLineDesc+"]");
//                        match.setFirstLineDesc(firstLineDesc);
//                    }
//			        
//			        String lineDesc = data.getString("line");
//			        if (!CommonUtil.isBlank(lineDesc) && !lineDesc.equals(match.getLine())) {
//			            updateLog.append(",line["+match.getLine()+" -> "+lineDesc+"]");
//			            match.setLineDesc(lineDesc);
//			        }
//			        
//			        String goalLineDesc = data.getString("goal_line");
//			        if (!CommonUtil.isBlank(goalLineDesc) && !goalLineDesc.equals(match.getGoalLineDesc())) {
//			            updateLog.append(",goalLine["+match.getGoalLineDesc()+" -> "+goalLineDesc+"]");
//			            match.setGoalLineDesc(goalLineDesc);
//			        }
//			        
//			        if (!CommonUtil.isBlank(goalLineDesc)) {
//			            Float goalLine = null;
//			            //若有 "/" 则取左右两个数字相加除以2
//			            if (goalLineDesc.contains("/")) {
//			                String[] arr = goalLineDesc.split("/");
//			                Float tmp = CommonUtil.addFloat(arr[0], arr[1]);
//			                goalLine = net.zweb.core.util.CommonUtil.div(tmp, 2).floatValue();
//			            } else {
//			                goalLine = CommonUtil.toFloat(goalLineDesc);
//			            }
//			            
//			            if (goalLine > 0 && !goalLine.equals(match.getGoalLine())) {
//	                        updateLog.append("goalLine["+match.getGoalLine()+" -> "+goalLine+"]");
//	                        match.setGoalLine(goalLine);
//	                    }
//			        }
//			        
//			        String status = data.getString("status");
//			        if (!CommonUtil.isBlank(status) && !status.equals(match.getStatus())) {
//                        updateLog.append(",status["+match.getStatus()+" -> "+status+"]");
//                        match.setStatus(status);
//                    }
//			        
//			        String winner = data.getString("winner");
//			        if (!CommonUtil.isBlank(winner) && !winner.equals(match.getWinner())) {
//			            updateLog.append(",winner["+match.getWinner()+" -> "+winner+"]");
//			            match.setWinner(winner);
//			        }
//			        int goals = data.getInt("goals");
//			        if (goals > 0 && goals != match.getGoals()) {
//			            updateLog.append(",goals["+match.getGoals()+" -> "+goals+"]");
//			            match.setGoals(goals);
//			        }
//			        String bigOrSmall = data.getString("big_or_small");
//			        if (!CommonUtil.isBlank(bigOrSmall) && !bigOrSmall.equals(match.getBigOrSmall())) {
//			            updateLog.append(",bigOrSmall["+match.getBigOrSmall()+" -> "+bigOrSmall+"]");
//			            match.setBigOrSmall(bigOrSmall);
//			        }
//			        String singleOrDouble = data.getString("single_or_double");
//			        if (!CommonUtil.isBlank(singleOrDouble) && !singleOrDouble.equals(match.getSingleOrDouble())) {
//			            updateLog.append(",singleOrDouble["+match.getSingleOrDouble()+" -> "+singleOrDouble+"]");
//			            match.setSingleOrDouble(singleOrDouble);
//			        }
//			        String halfOrFull = data.getString("half_or_full");
//			        if (!CommonUtil.isBlank(halfOrFull) && !halfOrFull.equals(match.getHalfOrFull())) {
//			            updateLog.append(",halfOrFull["+match.getHalfOrFull()+" -> "+halfOrFull+"]");
//			            match.setHalfOrFull(halfOrFull);
//			        }
//			        
//			        if (match.getId() != null && match.getId() > 0) {
//			            if (updateLog.length() > 0) {
//    			            db.save(match);
//    			            //记更新日志
//    	                    String json = JSON.toJSONString(model);
//    	                    Log log = new Log();
//    	                    log.setData(json);
//    	                    log.setMatch(match);
//    	                    log.setRemark(updateLog.toString());
//    	                    log.setTime(new Date());
//    	                    db.create(log);
//			            }
//			        } else {
//			            db.create(match);
//			        }
//			    }
//		};
//		
//		//启动爬虫 + 调度定时重启
//		Spiderman.me()
//			.listen(listener)//设置监听器
//			.schedule("10m")//调度，爬虫运行1m
//			.delay("10s")//每隔30分钟后重新抓取一次
//			.times(1)//调度 8 次，测试下4个小时内的数据变化
//			.startup()//启动
//			.blocking();//阻塞直到所有调度完成
//		
//		zweb.shutdown();
//		
//		System.exit(0);
//	}
//	
////	try {
////		List<String> pics = (List<String>) map.get("pics");
////		for (String pc : pics){
////			if (pc == null || pc.trim().length() == 0)
////				continue;
////			
////			final String pic = pc;
////			picPool.execute(new Runnable() {
////				public void run() {
////					try {
////						File file = new File(dir.getAbsoluteFile()+"/"+pic.replace("http://", "").replace("/", "_"));
////						ImageIO.write(FileUtil.getBufferedImage(pic, true, 1, 1*1000), "jpg", new FileOutputStream(file));
////						System.out.print("[SPIDERMAN] "+CommonUtil.getNowTime("HH:mm:ss")+" [INFO] ~ ");
////						System.out.println(file.getAbsolutePath() + " create finished...");
////					} catch (Exception e){
////						e.printStackTrace();
////					}
////				}
////			});
////		}
////	}catch(Exception e){
////		e.printStackTrace();
////	}
//	
//}
