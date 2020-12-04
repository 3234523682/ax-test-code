package com.ax.code.poi;

import cn.afterturn.easypoi.excel.annotation.Excel;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * @author lj
 */
@Data
public class MsgClient implements Serializable {

    @Excel(name = "电话号码", groupName = "联系方式", orderNum = "1")
    private String clientPhone = null;

    // 客户姓名
    @Excel(name = "姓名")
    private String clientName = null;

    // 备注
    @Excel(name = "备注")
    private String remark = null;

    // 生日
    @Excel(name = "出生日期", format = "yyyy-MM-dd", width = 20, groupName = "时间", orderNum = "2")
    private Date birthday = null;

    // 创建人
    @Excel(name = "创建时间", groupName = "时间", orderNum = "3")
    private String createBy = null;

    public MsgClient() {
    }

    public MsgClient(String clientPhone, String clientName) {
        this.clientPhone = clientPhone;
        this.clientName = clientName;
    }

    public MsgClient(String clientPhone, String clientName, String remark, Date birthday, String createBy) {
        this.clientPhone = clientPhone;
        this.clientName = clientName;
        this.remark = remark;
        this.birthday = birthday;
        this.createBy = createBy;
    }
}
