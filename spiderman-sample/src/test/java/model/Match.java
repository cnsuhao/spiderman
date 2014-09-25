//package model;
//
//import java.util.Date;
//
//import net.zweb.core.db.annotation.Column;
//import net.zweb.core.db.annotation.Id;
//import net.zweb.core.db.annotation.Table;
//
//@Table(text="比赛信息", name="sf_match")
//public class Match {
//
////    public final static Float 平手    = 0F;
////    public final static Float 平_半   = 0.25F;
////    public final static Float 半球      = 0.5F;
////    public final static Float 半_一   = 0.75F;
////    public final static Float 一球    = 1F;
////    public final static Float 一_球半 = 1.25F;
////    public final static Float 球半    = 1.5F;
////    public static Float line(String desc) {
////        if ("平手".equals(desc))
////            return Match.平手;
////        if ("平/半".equals(desc))
////            return Match.平_半;
////        if ("半球".equals(desc))
////            return Match.半球;
////        if ("半/一".equals(desc))
////            return Match.半_一;
////        if ("一球".equals(desc))
////            return Match.一球;
////        if ("一/球半".equals(desc))
////            return Match.一_球半;
////        if ("球半".equals(desc))
////            return Match.球半;
////        
////        return null;
////    }
////    
////    public static String lineDesc(Float line){
////        if (Match.平手.equals(line))
////            return "平手";
////        if (Match.平_半.equals(line))
////            return "平/半";
////        if (Match.半球.equals(line))
////            return "半";
////        if (Match.半_一.equals(line))
////            return "半/一";
////        if (Match.一球.equals(line))
////            return "一球";
////        if (Match.一_球半.equals(line))
////            return "一/球半";
////        if (Match.球半.equals(line))
////            return "球半";
////        
////        return null;
////    }
//    
//    @Id
//    @Column
//    private Long id;
//    
//    @Column(text="比赛分类", name="category_id")
//    private Category category;
//    
//    @Column(text="比赛时间")
//    private Date time;
//    
//    @Column(text="状态")
//    private String status;
//    
//    @Column(text="主队", name="home_team_id")
//    private Team homeTeam;
//    
//    @Column(text="主队入球", name="home_score")
//    private Integer homeScore;
//    
//    @Column(text="客队入球", name="visiting_score")
//    private Integer visitingScore;
//    public String score() {
//        return this.halfScore + "-" + this.visitingScore;
//    }
//    
//    @Column(text="客队", name="visiting_team_id")
//    private Team visitingTeam;
//    
//    @Column(text="半场比分", name="half_score")
//    private String halfScore;
//    
//    @Column(text="亚盘", name="goal_line")//大小球盘口
//    private Float goalLine;
//    
//    @Column(text="亚盘", name="goal_line_desc")//大小球盘口描述
//    private String goalLineDesc;
//    
//    @Column(text="赢盘")
//    private String winner;//上|下 来表示
//    
//    @Column(text="初盘", name="first_line")
//    private Float firstLine;
//    
//    @Column(text="初盘", name="first_line_desc")
//    private String firstLineDesc;
//    
//    @Column(text="盘口")
//    private Float line;
//    
//    @Column(text="盘口", name="line_desc")
//    private String lineDesc;
//    
//    @Column(text="入球数")
//    private int goals;
//    
//    @Column(text="大小", name="big_or_small")
//    private String bigOrSmall;
//    
//    @Column(text="单双", name="single_or_double")
//    private String singleOrDouble;
//    
//    @Column(text="半/全")
//    private String halfOrFull;
//    
//    public Long getId() {
//        return this.id;
//    }
//
//    public void setId(Long id) {
//        this.id = id;
//    }
//
//    public Category getCategory() {
//        return this.category;
//    }
//
//    public void setCategory(Category category) {
//        this.category = category;
//    }
//
//    public Date getTime() {
//        return this.time;
//    }
//
//    public void setTime(Date time) {
//        this.time = time;
//    }
//
//    public String getStatus() {
//        return this.status;
//    }
//
//    public void setStatus(String status) {
//        this.status = status;
//    }
//
//    public Team getHomeTeam() {
//        return this.homeTeam;
//    }
//
//    public void setHomeTeam(Team homeTeam) {
//        this.homeTeam = homeTeam;
//    }
//
//    public Team getVisitingTeam() {
//        return this.visitingTeam;
//    }
//
//    public void setVisitingTeam(Team visitingTeam) {
//        this.visitingTeam = visitingTeam;
//    }
//
//    public String getHalfScore() {
//        return this.halfScore;
//    }
//
//    public void setHalfScore(String halfScore) {
//        this.halfScore = halfScore;
//    }
//
//    public Integer getHomeScore() {
//        return this.homeScore;
//    }
//
//    public void setHomeScore(Integer homeScore) {
//        this.homeScore = homeScore;
//    }
//
//    public Integer getVisitingScore() {
//        return this.visitingScore;
//    }
//
//    public void setVisitingScore(Integer visitingScore) {
//        this.visitingScore = visitingScore;
//    }
//
//    public Float getLine() {
//        return this.line;
//    }
//
//    public void setLine(Float line) {
//        this.line = line;
//    }
//
//    public Float getGoalLine() {
//        return this.goalLine;
//    }
//
//    public void setGoalLine(Float goalLine) {
//        this.goalLine = goalLine;
//    }
//
//    public String getWinner() {
//        return this.winner;
//    }
//
//    public void setWinner(String winner) {
//        this.winner = winner;
//    }
//
//    public Float getFirstLine() {
//        return this.firstLine;
//    }
//
//    public void setFirstLine(Float firstLine) {
//        this.firstLine = firstLine;
//    }
//
//    public String getGoalLineDesc() {
//        return this.goalLineDesc;
//    }
//
//    public void setGoalLineDesc(String goalLineDesc) {
//        this.goalLineDesc = goalLineDesc;
//    }
//
//    public String getFirstLineDesc() {
//        return this.firstLineDesc;
//    }
//
//    public void setFirstLineDesc(String firstLineDesc) {
//        this.firstLineDesc = firstLineDesc;
//    }
//
//    public String getLineDesc() {
//        return this.lineDesc;
//    }
//
//    public void setLineDesc(String lineDesc) {
//        this.lineDesc = lineDesc;
//    }
//
//    public int getGoals() {
//        return this.goals;
//    }
//
//    public void setGoals(int goals) {
//        this.goals = goals;
//    }
//
//    public String getBigOrSmall() {
//        return this.bigOrSmall;
//    }
//
//    public void setBigOrSmall(String bigOrSmall) {
//        this.bigOrSmall = bigOrSmall;
//    }
//
//    public String getSingleOrDouble() {
//        return this.singleOrDouble;
//    }
//
//    public void setSingleOrDouble(String singleOrDouble) {
//        this.singleOrDouble = singleOrDouble;
//    }
//
//    public String getHalfOrFull() {
//        return this.halfOrFull;
//    }
//
//    public void setHalfOrFull(String halfOrFull) {
//        this.halfOrFull = halfOrFull;
//    }
//
//}
