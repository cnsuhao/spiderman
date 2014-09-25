//package model;
//
//import java.util.Date;
//
//import net.zweb.core.db.annotation.Column;
//import net.zweb.core.db.annotation.Id;
//import net.zweb.core.db.annotation.Table;
//
//@Table(text="实时更新日志", name="sf_log")
//public class Log {
//
//    @Id
//    @Column
//    private Long id;
//    
//    @Column(text="更新时间")
//    private Date time;
//    
//    @Column(text="数据包")
//    private String data;
//    
//    @Column(text="关联的比赛", name="match_id")
//    private Match match;
//    
//    @Column(text="备注")
//    private String remark;
//
//    public Long getId() {
//        return this.id;
//    }
//
//    public void setId(Long id) {
//        this.id = id;
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
//    public String getData() {
//        return this.data;
//    }
//
//    public void setData(String data) {
//        this.data = data;
//    }
//
//    public String getRemark() {
//        return this.remark;
//    }
//
//    public void setRemark(String remark) {
//        this.remark = remark;
//    }
//
//    public Match getMatch() {
//        return this.match;
//    }
//
//    public void setMatch(Match match) {
//        this.match = match;
//    }
//    
//}
