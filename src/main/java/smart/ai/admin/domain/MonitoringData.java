package smart.ai.admin.domain;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class MonitoringData {

    private String recId;
    private int hisSeq;
    private String dataId;
    private String sysDivCd;
    private int fileSz;
    private String filePth;
    private LocalDateTime pcsStDtm;
    private LocalDateTime pcsEdDtm;
    private String progStatCd;
    private String rsltStatCd;
    private String rsltStatMsg;
    private String threadId;
    private String wghtCd;

}
