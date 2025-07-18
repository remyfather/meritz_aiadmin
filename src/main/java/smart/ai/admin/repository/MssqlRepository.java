package smart.ai.admin.repository;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;


@Repository
public class MssqlRepository {

    private final JdbcTemplate jdbcTemplate;

    public MssqlRepository(@Qualifier("mssqlJdbcTemplate") JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    // public boolean checkList(String userid, String password) {
    //     String sql = """
    //         SELECT COUNT(1)
    //         -- FROM SMART.dbo.TB_USER
    //         FROM DBSTTS.dbo.TB_USER
    //         WHERE USER_ID = ?
    //           AND USER_PW = ?
    //     """;

    //     Integer count = jdbcTemplate.queryForObject(sql, Integer.class, userid, password);
    //     return count != null && count > 0;
    // }

    private String getDatetimeFormat(String unit) {

        return switch (unit) {
    
            case "day" -> "yyyy-MM-dd";
            case "hour" -> "yyyy-MM-dd HH";
            case "minute" -> "yyyy-MM-dd HH:mm";
            case "second" -> "yyyy-MM-dd HH:mm:ss";
    
            default -> throw new IllegalArgumentException("Invalid unit: " + unit);
    
        };
    
    }
    
    private String formatToMssqlDatetime(String yyyymmddHHmmss) {
    
        return yyyymmddHHmmss.replaceFirst(
            "(\\d{4})(\\d{2})(\\d{2})(\\d{2})(\\d{2})(\\d{2})",
            "$1-$2-$3 $4:$5:$6"
    
        );
    
    }

    public Mono<Boolean> isLatestError(String pcsId, int hisSeq) {
        String sql = """
            SELECT COUNT(1)
                        FROM DBSTTS.dbo.AIAPIMNG
                        WHERE PCS_ID = ?
                        AND HIS_SEQ = (SELECT MAX(HIS_SEQ)
                                        FROM DBSTTS.dbo.AIAPIMNG
                                        WHERE PCS_ID = ?
                                        )
                        AND HIS_SEQ = ?
                        AND PROG_STAT_CD = 'E'
        """;
    
        return Mono.fromCallable(() -> {
            Integer count = jdbcTemplate.queryForObject(sql, Integer.class, pcsId, pcsId, hisSeq);
            System.out.println(">>> count = " + count); 
            return count != null && count > 0;
        });
    }

    public Mono<Void> insertRetryRow(String pcsId, int hisSeq){
        return Mono.fromRunnable(() -> {
            String sql = """
                INSERT INTO DBSTTS.dbo.AIAPIMNG (PCS_ID, HIS_SEQ, PCS_ST_DTM, PCS_ED_DTM, FILE_SZ, SYS_DIV_CD, FILE_PTH, RSLT_STAT_CD, THREAD_ID, WGHT_CD) 
                SELECT PCS_ID,
                       HIS_SEQ+1 AS HIS_SEQ,
                       GETDATE() PCS_ST_DTM,
                       GETDATE() PCS_ED_DTM,
                       FILE_SZ, 
                       SYS_DIV_CD, 
                       FILE_PTH,
                       RSLT_STAT_CD,
                       THREAD_ID, 
                       WGHT_CD
                    FROM DBSTTS.dbo.AIAPIMNG
                    WHERE PCS_ID=trim(?)
                      AND HIS_SEQ=?
                    """;
            
            jdbcTemplate.update(sql, pcsId, hisSeq);
        });
    }

    public Mono<List<Map<String, Object>>> findGpuSnapshotUsage(String flag, Timestamp startT, Timestamp endT, String unitKeyword, String formatPattern){
 
        return Mono.fromCallable(() -> {

            // Timestamp startT = Timestamp.valueOf(start);
            // Timestamp endT = Timestamp.valueOf(end);

            String sql = ("""
            WITH time_slots AS (
                SELECT DATEADD(%s, n.number, CAST(? AS DATETIME)) AS slot
                FROM master..spt_values n WITH (NOLOCK)
                WHERE n.type = 'P'
                  AND n.number BETWEEN 0 AND DATEDIFF(%s, ?, ?)
            )
            SELECT
                t.slot AS time,
                e.flag, --ISNULL(e.flag, ?) AS flag,
                ISNULL(e.cnt, 0) AS cnt
            FROM time_slots t WITH (NOLOCK)
            LEFT JOIN (
                SELECT
                    DATEADD(%s, DATEDIFF(%s, 0, MOTR_DTM), 0) AS time_slot,
                    RSLT_STAT_CD AS flag,
                    MAX(CNT) AS cnt
                FROM DBSTTS.dbo.AIMOTRMNG WITH (NOLOCK)
                WHERE SYS_DIV_CD = ?
                  AND PROG_STAT_CD = 'C'
                  AND MOTR_DTM BETWEEN ? AND ?
                GROUP BY DATEADD(%s, DATEDIFF(%s, 0, MOTR_DTM), 0), RSLT_STAT_CD
            ) e
              ON t.slot = e.time_slot
            WHERE e.flag IS NOT NULL
            ORDER BY t.slot, flag
            """).formatted(unitKeyword, unitKeyword, unitKeyword, unitKeyword, unitKeyword, unitKeyword);
           
            List<Map<String, Object>> result = jdbcTemplate.queryForList(sql,
                    startT,       
                    startT, endT, 
                    flag,
                    startT, endT
            );

            // System.out.println("GPU 사용률 sql \n"+sql);

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern(formatPattern);
            for(Map<String, Object> row : result){
                row.put("time", formatter.format(((Timestamp) row.get("time")).toLocalDateTime()));
            }
           
            return result;

        })     
        .subscribeOn(Schedulers.boundedElastic());
    }

    public Mono<List<Map<String, Object>>> findGpuUtilizationPercent(

        String flag, Timestamp startT, Timestamp endT, String unitKeyword, String formatPattern) {

        return Mono.fromCallable(() -> {

            String sql = """
                SELECT TOP 100
                    FORMAT(MOTR_DTM, 'yyyy-MM-dd HH:mm:ss') AS time,
                    PROG_STAT_CD AS flag,
                    RSLT_STAT_CD AS div,
                    WGHT_CD AS id,
                    MAX(CNT) AS cnt
                FROM DBSTTS.dbo.AIMOTRMNG WITH (NOLOCK)
                WHERE SYS_DIV_CD = ? COLLATE Latin1_General_CS_AS
                AND MOTR_DTM BETWEEN ? AND ?
                GROUP BY
                    FORMAT(MOTR_DTM, 'yyyy-MM-dd HH:mm:ss'),
                    PROG_STAT_CD,
                    RSLT_STAT_CD,
                    WGHT_CD
                ORDER BY time DESC, div, flag
            """;
            List<Map<String, Object>> result = jdbcTemplate.queryForList(
                sql,
                flag, startT, endT
            );

            return result;

        }).subscribeOn(Schedulers.boundedElastic());

    }

    public Mono<List<Map<String, Object>>> findGpuMemoryUsage(

        String flag, Timestamp startT, Timestamp endT, String unitKeyword, String formatPattern) {

        return Mono.fromCallable(() -> {

            String sql = """
                SELECT TOP 100
                    FORMAT(MOTR_DTM, 'yyyy-MM-dd HH:mm:ss') AS time,
                    PROG_STAT_CD AS flag,
                    RSLT_STAT_CD AS div,
                    WGHT_CD AS id,
                    MAX(CNT) AS cnt
                FROM DBSTTS.dbo.AIMOTRMNG WITH (NOLOCK)
                WHERE SYS_DIV_CD = ? COLLATE Latin1_General_CS_AS
                AND MOTR_DTM BETWEEN ? AND ?
            GROUP BY
                    FORMAT(MOTR_DTM, 'yyyy-MM-dd HH:mm:ss'),
                    PROG_STAT_CD,
                    RSLT_STAT_CD,
                    WGHT_CD
                ORDER BY time DESC, div, flag
            """;

            List<Map<String, Object>> result = jdbcTemplate.queryForList(
                sql,
                flag, startT, endT
            );

            return result;

        }).subscribeOn(Schedulers.boundedElastic());

    }

    public Mono<String> findPeakCountDate(String div, String unitKeyword, String aggType) {

        String sql = """
            WITH BASE AS (
                SELECT CAST(time_slot as date) time_slot
                        , SUM(CNT) cnt
                FROM (
                SELECT
                    DATEADD(%s, DATEDIFF(%s, 0, PCS_ST_DTM), 0) AS time_slot,
                    SYS_DIV_CD AS div,
                    COUNT(*) cnt
                FROM DBSTTS.dbo.AIAPIMNG WITH (NOLOCK)
                WHERE 1=1
                AND PROG_STAT_CD = 'C' COLLATE Latin1_General_CS_AS
                AND PCS_ST_DTM >= '2025-05-09'
                AND PCS_ST_DTM >= DATEADD(MONTH, -1, GETDATE()) -- 최근 1달이내
                AND DAY(PCS_ST_DTM) >= DAY(EOMONTH(PCS_ST_DTM)) - 5 -- 각 달의 마지막 5일
                AND SYS_DIV_CD = ?
                GROUP BY DATEADD(%s, DATEDIFF(%s, 0, PCS_ST_DTM), 0), SYS_DIV_CD
                ) a
                GROUP BY CAST(time_slot as date)
                )       
                , RANKED AS (
                SELECT *
                        , ROW_NUMBER() OVER( ORDER BY CNT desc ) rnk
                    FROM BASE
                )       
                SELECT time_slot
                FROM RANKED
                WHERE RNK = 1
                """.formatted(unitKeyword, unitKeyword, unitKeyword, unitKeyword);

        System.out.println("peak 확인 sql \n"+sql);
    
        return Mono.fromCallable(() -> {
            String peakDate = jdbcTemplate.queryForObject(sql, String.class, div);
            return peakDate;
        });
    }

    public Mono<List<Map<String, Object>>> findAiDaemonFlagCount(String div, Timestamp startT, Timestamp endT, String unitKeyword, String formatPattern, String aggType){
 
        return Mono.fromCallable(() -> {

            String sql = "";

            if("max".equals(aggType)){
                sql = ("""
                    WITH time_slots AS (
                        SELECT DATEADD(%s, n.number, CAST(? AS DATETIME)) AS slot
                        FROM master..spt_values n WITH (NOLOCK)
                        WHERE n.type = 'P'
                          AND n.number BETWEEN 0 AND DATEDIFF(%s, ?, ?)
                    )
                    SELECT
                        t.slot AS time,
                        div,
                        ISNULL(e.cnt, 0) AS cnt
                    FROM time_slots t WITH (NOLOCK)
                    LEFT JOIN (
                        SELECT time_slot
                         , div
                         , SUM(cnt) cnt
                      FROM (
                        SELECT
                            DATEADD(%s, DATEDIFF(%s, 0, MOTR_DTM), 0) AS time_slot,
                            SYS_DIV_CD AS div,
                            MAX(CNT) AS cnt
                        FROM DBSTTS.dbo.AIMOTRMNG WITH (NOLOCK)
                        WHERE ((SYS_DIV_CD = 'C' AND WGHT_CD = '41') OR (SYS_DIV_CD = 'T' AND WGHT_CD = '33'))
                          AND MOTR_DTM BETWEEN ? AND ?
                          AND SYS_DIV_CD = ?
                        GROUP BY DATEADD(%s, DATEDIFF(%s, 0, MOTR_DTM), 0), SYS_DIV_CD
                        ) a
                        GROUP BY time_slot, div
                    ) e
                      ON t.slot = e.time_slot
                      WHERE div is not null
                    ORDER BY t.slot
                    """).formatted(unitKeyword, unitKeyword, unitKeyword, unitKeyword, unitKeyword, unitKeyword, unitKeyword);
            } else {
                sql = ("""
                    WITH time_slots AS (
                        SELECT DATEADD(%s, n.number, CAST(? AS DATETIME)) AS slot
                        FROM master..spt_values n WITH (NOLOCK)
                        WHERE n.type = 'P'
                          AND n.number BETWEEN 0 AND DATEDIFF(%s, ?, ?)
                    )
                    SELECT
                        t.slot AS time,
                        div,
                        ISNULL(e.cnt, 0) AS cnt
                    FROM time_slots t WITH (NOLOCK)
                    LEFT JOIN (
                        SELECT time_slot
                         , div
                         , SUM(cnt) cnt
                      FROM (
                        SELECT
                            DATEADD(%s, DATEDIFF(%s, 0, MOTR_DTM), 0) AS time_slot,
                            SYS_DIV_CD AS div,
                            AVG(CNT) AS cnt
                        FROM DBSTTS.dbo.AIMOTRMNG WITH (NOLOCK)
                        WHERE ((SYS_DIV_CD = 'C' AND WGHT_CD = '41') OR (SYS_DIV_CD = 'T' AND WGHT_CD = '33'))
                          AND MOTR_DTM BETWEEN ? AND ?
                          AND SYS_DIV_CD = ?
                        GROUP BY DATEADD(%s, DATEDIFF(%s, 0, MOTR_DTM), 0), SYS_DIV_CD
                        ) a
                        GROUP BY time_slot, div
                    ) e
                      ON t.slot = e.time_slot
                      WHERE div is not null
                    ORDER BY t.slot
                    """).formatted(unitKeyword, unitKeyword, unitKeyword, unitKeyword, unitKeyword, unitKeyword, unitKeyword);
            }

            
           
            List<Map<String, Object>> result = jdbcTemplate.queryForList(sql,
                    startT,       
                    startT, endT, 
                    startT, endT,
                    div
            );

            // System.out.println("STT 처리중 sql \n"+sql);

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern(formatPattern);
            for(Map<String, Object> row : result){
                row.put("time", formatter.format(((Timestamp) row.get("time")).toLocalDateTime()));
            }
           
            return result;
        }).subscribeOn(Schedulers.boundedElastic());
    }

    public Mono<List<Map<String, Object>>> findAiDaemonFlagCount2(String div, Timestamp startT, Timestamp endT, String unitKeyword, String formatPattern, String aggType) {
 
        return Mono.fromCallable(() -> {

            String sql = "";

            if("max".equals(aggType)){
                sql = ("""
                    WITH time_slots AS (
                        SELECT DATEADD(%s, n.number, CAST(? AS DATETIME)) AS slot
                        FROM master..spt_values n WITH (NOLOCK)
                        WHERE n.type = 'P'
                          AND n.number BETWEEN 0 AND DATEDIFF(%s, ?, ?)
                    )
                    SELECT
                        t.slot AS time,
                        div,
                        ISNULL(e.cnt, 0) AS cnt
                    FROM time_slots t WITH (NOLOCK)
                    LEFT JOIN (
                        SELECT time_slot
                         , div
                         , SUM(cnt) cnt
                      FROM (
                        SELECT
                            DATEADD(%s, DATEDIFF(%s, 0, MOTR_DTM), 0) AS time_slot,
                            SYS_DIV_CD AS div,
                            MAX(CNT) AS cnt
                        FROM DBSTTS.dbo.AIMOTRMNG WITH (NOLOCK)
                        WHERE ((SYS_DIV_CD = 'C' AND PROG_STAT_CD = 'S') OR (SYS_DIV_CD = 'T' AND PROG_STAT_CD = 'S'))
                          AND PROG_STAT_CD = 'S' COLLATE Latin1_General_CS_AS
                          AND MOTR_DTM BETWEEN ? AND ?
                          AND SYS_DIV_CD = ?
                        GROUP BY DATEADD(%s, DATEDIFF(%s, 0, MOTR_DTM), 0), SYS_DIV_CD
                        ) a
                        GROUP BY time_slot, div
                    ) e
                      ON t.slot = e.time_slot
                      WHERE div is not null
                    ORDER BY t.slot
                    """).formatted(unitKeyword, unitKeyword, unitKeyword, unitKeyword, unitKeyword, unitKeyword, unitKeyword);
            } else {
                sql = ("""
                    WITH time_slots AS (
                        SELECT DATEADD(%s, n.number, CAST(? AS DATETIME)) AS slot
                        FROM master..spt_values n WITH (NOLOCK)
                        WHERE n.type = 'P'
                          AND n.number BETWEEN 0 AND DATEDIFF(%s, ?, ?)
                    )
                    SELECT
                        t.slot AS time,
                        div,
                        ISNULL(e.cnt, 0) AS cnt
                    FROM time_slots t WITH (NOLOCK)
                    LEFT JOIN (
                        SELECT time_slot
                         , div
                         , SUM(cnt) cnt
                      FROM (
                        SELECT
                            DATEADD(%s, DATEDIFF(%s, 0, MOTR_DTM), 0) AS time_slot,
                            SYS_DIV_CD AS div,
                            AVG(CNT) AS cnt
                        FROM DBSTTS.dbo.AIMOTRMNG WITH (NOLOCK)
                        WHERE ((SYS_DIV_CD = 'C' AND PROG_STAT_CD = 'S') OR (SYS_DIV_CD = 'T' AND PROG_STAT_CD = 'S'))
                          AND PROG_STAT_CD = 'S' COLLATE Latin1_General_CS_AS
                          AND MOTR_DTM BETWEEN ? AND ?
                          AND SYS_DIV_CD = ?
                        GROUP BY DATEADD(%s, DATEDIFF(%s, 0, MOTR_DTM), 0), SYS_DIV_CD
                        ) a
                        GROUP BY time_slot, div
                    ) e
                      ON t.slot = e.time_slot
                      WHERE div is not null
                    ORDER BY t.slot
                    """).formatted(unitKeyword, unitKeyword, unitKeyword, unitKeyword, unitKeyword, unitKeyword, unitKeyword);
            }

            List<Map<String, Object>> result = jdbcTemplate.queryForList(sql,
                    startT,       
                    startT, endT, 
                    startT, endT,
                    div
            );

            // System.out.println("데몬2 처리중 sql \n"+sql);

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern(formatPattern);
            for(Map<String, Object> row : result){
                row.put("time", formatter.format(((Timestamp) row.get("time")).toLocalDateTime()));
            }
           
            return result;
        }).subscribeOn(Schedulers.boundedElastic());
    }

    public Mono<List<Map<String, Object>>> findAiDaemonFlagCount3(String div, Timestamp startT, Timestamp endT, String unitKeyword, String formatPattern, String aggType) {
 
        return Mono.fromCallable(() -> {

            String sql = "";

            if("max".equals(aggType)){
                sql = ("""
                    WITH time_slots AS (
                        SELECT DATEADD(%s, n.number, CAST(? AS DATETIME)) AS slot
                        FROM master..spt_values n WITH (NOLOCK)
                        WHERE n.type = 'P'
                          AND n.number BETWEEN 0 AND DATEDIFF(%s, ?, ?)
                    )
                    SELECT
                        t.slot AS time,
                        div,
                        ISNULL(e.cnt, 0) AS cnt
                    FROM time_slots t WITH (NOLOCK)
                    LEFT JOIN (
                        SELECT time_slot
                             , div
                             , SUM(cnt) cnt
                          FROM (
                                SELECT
                                    DATEADD(%s, DATEDIFF(%s, 0, MOTR_DTM), 0) AS time_slot,
                                    SYS_DIV_CD AS div,
                                    WGHT_CD,
                                    MAX(CNT) AS cnt
                                FROM DBSTTS.dbo.AIMOTRMNG WITH (NOLOCK)
                                WHERE ((SYS_DIV_CD = 'C' AND PROG_STAT_CD = 'E') OR (SYS_DIV_CD = 'T' AND PROG_STAT_CD = 'E'))
                                AND MOTR_DTM BETWEEN ? AND ?
                                AND SYS_DIV_CD = ?
                                GROUP BY DATEADD(%s, DATEDIFF(%s, 0, MOTR_DTM), 0), SYS_DIV_CD, WGHT_CD
                        ) a
                        GROUP BY time_slot, div
                    ) e
                      ON t.slot = e.time_slot
                      WHERE div is not null
                    ORDER BY t.slot
                    """).formatted(unitKeyword, unitKeyword, unitKeyword, unitKeyword, unitKeyword, unitKeyword, unitKeyword);        
            } else {
                sql = ("""
                    WITH time_slots AS (
                        SELECT DATEADD(%s, n.number, CAST(? AS DATETIME)) AS slot
                        FROM master..spt_values n WITH (NOLOCK)
                        WHERE n.type = 'P'
                          AND n.number BETWEEN 0 AND DATEDIFF(%s, ?, ?)
                    )
                    SELECT
                        t.slot AS time,
                        div,
                        ISNULL(e.cnt, 0) AS cnt
                    FROM time_slots t WITH (NOLOCK)
                    LEFT JOIN (
                        SELECT time_slot
                             , div
                             , SUM(cnt) cnt
                          FROM (
                                SELECT
                                    DATEADD(%s, DATEDIFF(%s, 0, MOTR_DTM), 0) AS time_slot,
                                    SYS_DIV_CD AS div,
                                    WGHT_CD,
                                    AVG(CNT) AS cnt
                                FROM DBSTTS.dbo.AIMOTRMNG WITH (NOLOCK)
                                WHERE ((SYS_DIV_CD = 'C' AND PROG_STAT_CD = 'E') OR (SYS_DIV_CD = 'T' AND PROG_STAT_CD = 'E'))
                                AND MOTR_DTM BETWEEN ? AND ?
                                AND SYS_DIV_CD = ?
                                GROUP BY DATEADD(%s, DATEDIFF(%s, 0, MOTR_DTM), 0), SYS_DIV_CD, WGHT_CD
                        ) a
                        GROUP BY time_slot, div
                    ) e
                      ON t.slot = e.time_slot
                      WHERE div is not null
                    ORDER BY t.slot
                    """).formatted(unitKeyword, unitKeyword, unitKeyword, unitKeyword, unitKeyword, unitKeyword, unitKeyword);        
            }


            List<Map<String, Object>> result = jdbcTemplate.queryForList(sql,
                    startT,       
                    startT, endT, 
                    startT, endT,
                    div
            );

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern(formatPattern);
            for(Map<String, Object> row : result){
                row.put("time", formatter.format(((Timestamp) row.get("time")).toLocalDateTime()));
            }
           
            return result;

        }).subscribeOn(Schedulers.boundedElastic());
    }

    public List<Map<String, Object>> findDataByDate(LocalDate date) {
        LocalDateTime start = date.atStartOfDay();
        LocalDateTime end = date.plusDays(1).atStartOfDay();

        String sql = """
            SELECT *
                FROM (
                select A.REC_ID,
                    --A.PROG_STAT_CD AS '상태코드',
                    --A.RECORD_START_DATE AS '처리일자',
                    --A.RECORD_START_TIME AS '녹취시작',
                    --A.RECORD_END_TIME AS '녹취끝',
                    A.RECORD_DURATION, -- AS '녹취시간',
                    A.CNVR_ST_DTM, -- 'STT변환준비',
                    A.STT_ST_DTM, -- 'STT시작',
                    A.STT_ED_DTM, -- 'STT끝',
                    DATEDIFF(SECOND, A.CNVR_ST_DTM, A.STT_ED_DTM) STT_TOTAL_DURATION, -- AS 'STT전체처리시간',
                    DATEDIFF(SECOND, A.STT_ST_DTM, A.STT_ED_DTM) STT_DURATION, -- 'STT처리시간',
                    --B.STMT_CNT,
                    C.PCS_ST_DTM, -- AS '요약시작',
                    C.PCS_ED_DTM, -- AS '요약끝',
                    DATEDIFF(SECOND, C.PCS_ST_DTM, C.PCS_ED_DTM) SMRY_DURATION, -- AS '요약처리시간',
                    C.PROG_STAT_CD, -- S 큐에적재, R 처리 중, C 완료, E 에러
                    --CASE WHEN C.PROG_STAT_CD = 'S' THEN 'S 큐에적재'
                    --     WHEN C.PROG_STAT_CD = 'R' THEN 'R 처리중'
                    --     WHEN C.PROG_STAT_CD = 'C' THEN 'C 완료'
                    --     WHEN C.PROG_STAT_CD = 'E' THEN 'E 에러**'    
                    --     ELSE '' END AS '요약상태코드',
                    C.RSLT_STAT_MSG, C.WGHT_CD,
                    C.HS,
                    DATEDIFF(SECOND, A.CNVR_ST_DTM, A.STT_ED_DTM) + DATEDIFF(SECOND, C.PCS_ST_DTM, C.PCS_ED_DTM) TOTAL_EXCUTION_TIME -- AS 'Total Excution Time'
                --       CAST(DATEDIFF(SECOND, A.CNVR_ST_DTM, C.PCS_ED_DTM)/3600 AS VARCHAR) + ':' + CAST(((DATEDIFF(SECOND, A.CNVR_ST_DTM, C.PCS_ED_DTM))%3600)/60 AS VARCHAR) + ':' + CAST((DATEDIFF(SECOND, A.CNVR_ST_DTM, C.PCS_ED_DTM))%60 AS VARCHAR)  AS 'Overall Processing Period' 
                FROM DBSTTS.dbo.STTRECSTAT A with (nolock),  
                (   select rec_id, max(STMT_NO) as stmt_cnt 
                    from(	
                        SELECT rec_id, CAST(STMT_NO AS INT) AS STMT_NO
                        FROM DBSTTS.dbo.STTRECRSTINF with (nolock) -- statement
                        where CRTPE_DTM BETWEEN ? AND ? -- >= CAST(GETDATE() AS DATE)
                    )D
                    where 1=1
                    group by rec_id
                ) B ,
                (
                select T.PCS_ID, MAX(T.HIS_SEQ) AS 'HS', T.PROG_STAT_CD, T.RSLT_STAT_MSG, T.WGHT_CD, T.PCS_ST_DTM, T.PCS_ED_DTM
                    FROM DBSTTS.dbo.AIAPIMNG T with (nolock)
                    where 1=1 
                    and T.PCS_ST_DTM BETWEEN ? AND ? -- >= CAST(GETDATE() AS DATE)
                    and T.SYS_DIV_CD = 'C'
                    group by T.PCS_ID, T.PROG_STAT_CD, T.RSLT_STAT_MSG, T.WGHT_CD, T.PCS_ST_DTM, T.PCS_ED_DTM
                ) C	
                where 1=1
                and A.REC_ID = B.REC_ID
                and A.REC_ID = C.PCS_ID
                and CNVR_ST_DTM BETWEEN ? AND ? -- >= CAST(GETDATE() AS DATE)
                and C.PROG_STAT_CD IS NOT NULL
                --and C.PROG_STAT_CD = 'S'
                --order by CNVR_ST_DTM DESC
                UNION
                select 
                    A.REC_ID,
                    --A.PROG_STAT_CD AS '상태코드',
                    --A.RECORD_START_DATE AS '처리일자',
                    --A.RECORD_START_TIME AS '녹취시작',
                    --A.RECORD_END_TIME AS '녹취끝',
                    A.RECORD_DURATION RECORD_DURATION, -- AS '녹취시간',
                    A.CNVR_ST_DTM, -- AS 'STT변환준비',
                    A.STT_ST_DTM, -- AS 'STT시작',
                    A.STT_ED_DTM, -- AS 'STT끝',
                    DATEDIFF(SECOND, A.CNVR_ST_DTM, A.STT_ED_DTM) STT_TOTAL_DURATION, -- AS 'STT전체처리시간',
                    DATEDIFF(SECOND, A.STT_ST_DTM, A.STT_ED_DTM) STT_DURATION, -- AS 'STT처리시간',
                    --B.STMT_CNT,
                    C.PCS_ST_DTM, -- AS '요약시작',
                    C.PCS_ED_DTM, -- AS '요약끝',
                    DATEDIFF(SECOND, C.PCS_ST_DTM, C.PCS_ED_DTM) SMRY_DURATION, -- AS '요약처리시간',
                    C.PROG_STAT_CD, -- S 큐에적재, R 처리 중, C 완료, E 에러
                    -- CASE WHEN C.PROG_STAT_CD = 'S' THEN 'S 큐에적재'
                    --      WHEN C.PROG_STAT_CD = 'R' THEN 'R 처리중'
                    --      WHEN C.PROG_STAT_CD = 'C' THEN 'C 완료'
                    --      WHEN C.PROG_STAT_CD = 'E' THEN 'E 에러**'    
                    --      ELSE '' END AS '요약상태코드',
                    C.RSLT_STAT_MSG, C.WGHT_CD,
                    C.HS,
                    DATEDIFF(SECOND, A.CNVR_ST_DTM, A.STT_ED_DTM) + DATEDIFF(SECOND, C.PCS_ST_DTM, C.PCS_ED_DTM) TOTAL_EXCUTION_TIME -- AS 'Total Excution Time'
                --       CAST(DATEDIFF(SECOND, A.CNVR_ST_DTM, C.PCS_ED_DTM)/3600 AS VARCHAR) + ':' + CAST(((DATEDIFF(SECOND, A.CNVR_ST_DTM, C.PCS_ED_DTM))%3600)/60 AS VARCHAR) + ':' + CAST((DATEDIFF(SECOND, A.CNVR_ST_DTM, C.PCS_ED_DTM))%60 AS VARCHAR)  AS 'Overall Processing Period' 
                FROM DBSTTS.dbo.STTARECSTAT A with (nolock),  
                (   select rec_id, max(STMT_NO) as stmt_cnt 
                    from(	
                        SELECT rec_id, CAST(STMT_NO AS INT) AS STMT_NO
                        FROM DBSTTS.dbo.STTARECRSTINF with (nolock) -- statement
                        where CRTPE_DTM BETWEEN ? AND ? -- >= CAST(DATEADD(DAY, -1, GETDATE()) AS DATE)
                    )D
                    where 1=1
                    group by rec_id
                ) B ,
                (
                select T.PCS_ID, MAX(T.HIS_SEQ) AS 'HS', T.PROG_STAT_CD, T.RSLT_STAT_MSG, T.WGHT_CD, T.PCS_ST_DTM, T.PCS_ED_DTM
                    FROM DBSTTS.dbo.AIAPIMNG T with (nolock)
                    where 1=1 
                    and T.PCS_ST_DTM BETWEEN ? AND ? -- >= CAST(DATEADD(DAY, -1, GETDATE()) AS DATE)
                    and T.SYS_DIV_CD = 'T'
                    group by T.PCS_ID, T.PROG_STAT_CD, T.RSLT_STAT_MSG, T.WGHT_CD, T.PCS_ST_DTM, T.PCS_ED_DTM
                ) C	
                where 1=1
                and A.REC_ID = B.REC_ID
                and A.REC_ID = C.PCS_ID
                and CNVR_ST_DTM BETWEEN ? AND ? -- >= CAST(DATEADD(DAY, -1, GETDATE()) AS DATE)
                and C.PROG_STAT_CD IS NOT NULL
                --order by CNVR_ST_DTM DESC
                --OFFSET 0 ROWS
                --FETCH NEXT 100 ROWS ONLY
                ) base
               -- ORDER BY CNVR_ST_DTM DESC
            """
            ;
        return jdbcTemplate.queryForList(
            sql,
            Timestamp.valueOf(start), Timestamp.valueOf(end),
            Timestamp.valueOf(start), Timestamp.valueOf(end),
            Timestamp.valueOf(start), Timestamp.valueOf(end),
            Timestamp.valueOf(start), Timestamp.valueOf(end),
            Timestamp.valueOf(start), Timestamp.valueOf(end),
            Timestamp.valueOf(start), Timestamp.valueOf(end)
        );
    }

    public Mono<List<Map<String, Object>>> findDetailData(String start, String end, int limit, String progStatCd, String sysDivCd, String lastDate) {

        String sql = String.format("""
            SELECT TOP %d *
                FROM (
                select A.REC_ID,
                    --A.PROG_STAT_CD AS '상태코드',
                    --A.RECORD_START_DATE AS '처리일자',
                    --A.RECORD_START_TIME AS '녹취시작',
                    --A.RECORD_END_TIME AS '녹취끝',
                    A.RECORD_DURATION, -- AS '녹취시간',
                    A.CNVR_ST_DTM, -- 'STT변환준비',
                    A.STT_ST_DTM, -- 'STT시작',
                    A.STT_ED_DTM, -- 'STT끝',
                    DATEDIFF(SECOND, A.CNVR_ST_DTM, A.STT_ED_DTM) STT_TOTAL_DURATION, -- AS 'STT전체처리시간',
                    DATEDIFF(SECOND, A.STT_ST_DTM, A.STT_ED_DTM) STT_DURATION, -- 'STT처리시간',
                    --B.STMT_CNT,
                    C.PCS_ST_DTM, -- AS '요약시작',
                    C.PCS_ED_DTM, -- AS '요약끝',
                    DATEDIFF(SECOND, C.PCS_ST_DTM, C.PCS_ED_DTM) SMRY_DURATION, -- AS '요약처리시간',
                    C.PROG_STAT_CD, -- S 큐에적재, R 처리 중, C 완료, E 에러
                    --CASE WHEN C.PROG_STAT_CD = 'S' THEN 'S 큐에적재'
                    --     WHEN C.PROG_STAT_CD = 'R' THEN 'R 처리중'
                    --     WHEN C.PROG_STAT_CD = 'C' THEN 'C 완료'
                    --     WHEN C.PROG_STAT_CD = 'E' THEN 'E 에러**'    
                    --     ELSE '' END AS '요약상태코드',
                    C.RSLT_STAT_MSG, C.WGHT_CD,
                    C.HS,
                    C.RETRY_ENABLE_YN,
                    DATEDIFF(SECOND, A.CNVR_ST_DTM, A.STT_ED_DTM) + DATEDIFF(SECOND, C.PCS_ST_DTM, C.PCS_ED_DTM) TOTAL_EXCUTION_TIME -- AS 'Total Excution Time'
                FROM DBSTTS.dbo.STTRECSTAT A with (nolock),  
                --(   select rec_id, max(STMT_NO) as stmt_cnt 
                --    from(	
                --        SELECT rec_id, CAST(STMT_NO AS INT) AS STMT_NO
                --        FROM DBSTTS.dbo.STTRECRSTINF with (nolock) -- statement
                --        where CRTPE_DTM >= ?
                --          AND CRTPE_DTM < ?
                --    )D
                --    where 1=1
                --    group by rec_id
                --) B ,
                (
                select T.PCS_ID
                     , T.HIS_SEQ AS 'HS'
                     , T.PROG_STAT_CD
                     , T.RSLT_STAT_MSG
                     , T.WGHT_CD
                     , T.PCS_ST_DTM
                     , T.PCS_ED_DTM
                     , CASE WHEN T.PROG_STAT_CD = 'E'
                            THEN 'Y'
                            ELSE 'N'
                             END RETRY_ENABLE_YN
                -- select T.PCS_ID, MAX(T.HIS_SEQ) AS 'HS', T.PROG_STAT_CD, T.RSLT_STAT_MSG, T.WGHT_CD, T.PCS_ST_DTM, T.PCS_ED_DTM
                    FROM DBSTTS.dbo.AIAPIMNG T with (nolock)
                    where 1=1 
                    and T.PCS_ST_DTM >= ?
                    AND T.PCS_ST_DTM < ?
                    and T.SYS_DIV_CD = 'C'
                    AND T.HIS_SEQ = (SELECT MAX(T2.HIS_SEQ)
                                       FROM DBSTTS.dbo.AIAPIMNG T2 with (nolock)
                                       WHERE T2.PCS_ID = T.PCS_ID
                                    )
                    AND (? IS NULL OR PROG_STAT_CD = ?)
                    AND (? IS NULL OR SYS_DIV_CD = ?)
                -- group by T.PCS_ID, T.PROG_STAT_CD, T.RSLT_STAT_MSG, T.WGHT_CD, T.PCS_ST_DTM, T.PCS_ED_DTM
                ) C	
                where 1=1
                --and A.REC_ID = B.REC_ID
                and A.REC_ID = C.PCS_ID
                ----and CNVR_ST_DTM >= ?
                ----AND CNVR_ST_DTM < ?
                and C.PROG_STAT_CD IS NOT NULL
                AND (? IS NULL OR A.CNVR_ST_DTM < ?)                
                UNION
                select 
                    A.REC_ID,
                    --A.PROG_STAT_CD AS '상태코드',
                    --A.RECORD_START_DATE AS '처리일자',
                    --A.RECORD_START_TIME AS '녹취시작',
                    --A.RECORD_END_TIME AS '녹취끝',
                    A.RECORD_DURATION RECORD_DURATION, -- AS '녹취시간',
                    A.CNVR_ST_DTM, -- AS 'STT변환준비',
                    A.STT_ST_DTM, -- AS 'STT시작',
                    A.STT_ED_DTM, -- AS 'STT끝',
                    DATEDIFF(SECOND, A.CNVR_ST_DTM, A.STT_ED_DTM) STT_TOTAL_DURATION, -- AS 'STT전체처리시간',
                    DATEDIFF(SECOND, A.STT_ST_DTM, A.STT_ED_DTM) STT_DURATION, -- AS 'STT처리시간',
                    --B.STMT_CNT,
                    C.PCS_ST_DTM, -- AS '요약시작',
                    C.PCS_ED_DTM, -- AS '요약끝',
                    DATEDIFF(SECOND, C.PCS_ST_DTM, C.PCS_ED_DTM) SMRY_DURATION, -- AS '요약처리시간',
                    C.PROG_STAT_CD, -- S 큐에적재, R 처리 중, C 완료, E 에러
                    -- CASE WHEN C.PROG_STAT_CD = 'S' THEN 'S 큐에적재'
                    --      WHEN C.PROG_STAT_CD = 'R' THEN 'R 처리중'
                    --      WHEN C.PROG_STAT_CD = 'C' THEN 'C 완료'
                    --      WHEN C.PROG_STAT_CD = 'E' THEN 'E 에러**'    
                    --      ELSE '' END AS '요약상태코드',
                    C.RSLT_STAT_MSG, C.WGHT_CD,
                    C.HS,
                    C.RETRY_ENABLE_YN,
                    DATEDIFF(SECOND, A.CNVR_ST_DTM, A.STT_ED_DTM) + DATEDIFF(SECOND, C.PCS_ST_DTM, C.PCS_ED_DTM) TOTAL_EXCUTION_TIME -- AS 'Total Excution Time'
                FROM DBSTTS.dbo.STTARECSTAT A with (nolock),  
                --(   select rec_id, max(STMT_NO) as stmt_cnt 
                --    from(	
                --        SELECT rec_id, CAST(STMT_NO AS INT) AS STMT_NO
                --        FROM DBSTTS.dbo.STTARECRSTINF with (nolock) -- statement
                --        where CRTPE_DTM >= ?
                --          AND CRTPE_DTM < ?
                --    )D
                --    where 1=1
                --    group by rec_id
                --) B ,
                (
                select T.PCS_ID
                     , T.HIS_SEQ AS 'HS'
                     , T.PROG_STAT_CD
                     , T.RSLT_STAT_MSG
                     , T.WGHT_CD
                     , T.PCS_ST_DTM
                     , T.PCS_ED_DTM
                     ,  CASE WHEN T.PROG_STAT_CD = 'E'
                                THEN 'Y'
                                ELSE 'N'
                                END RETRY_ENABLE_YN
                -- select T.PCS_ID, MAX(T.HIS_SEQ) AS 'HS', T.PROG_STAT_CD, T.RSLT_STAT_MSG, T.WGHT_CD, T.PCS_ST_DTM, T.PCS_ED_DTM
                    FROM DBSTTS.dbo.AIAPIMNG T with (nolock)
                    where 1=1 
                    and T.PCS_ST_DTM >= ?
                    AND T.PCS_ST_DTM < ?
                    and T.SYS_DIV_CD = 'T'
                    AND T.HIS_SEQ = (SELECT MAX(T2.HIS_SEQ)
                                       FROM DBSTTS.dbo.AIAPIMNG T2 with (nolock)
                                       WHERE T2.PCS_ID = T.PCS_ID
                                    )
                    AND (? IS NULL OR PROG_STAT_CD = ?)
                    AND (? IS NULL OR SYS_DIV_CD = ?)    
                --   group by T.PCS_ID, T.PROG_STAT_CD, T.RSLT_STAT_MSG, T.WGHT_CD, T.PCS_ST_DTM, T.PCS_ED_DTM
                ) C	
                where 1=1
                --and A.REC_ID = B.REC_ID
                and A.REC_ID = C.PCS_ID
                ----and CNVR_ST_DTM >= ?
                ----AND CNVR_ST_DTM < ?
                and C.PROG_STAT_CD IS NOT NULL
                AND (? IS NULL OR A.CNVR_ST_DTM < ?)
                ) base
                WHERE (? IS NULL OR base.PCS_ST_DTM < ?)
                ORDER BY PCS_ST_DTM DESC
            """, limit);

        String startStr = start.replace("T", " ");
        String endStr = end.replace("T", " ");

        // 날짜 전처리
        // 기준이 PCS_ST_DTM 이라서 STT 관련은 1일 이전으로 조회 기준일 변경함
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        String crtpeStart = LocalDateTime.parse(startStr, formatter)
                                            // .minusHours(1)
                                            .minusDays(1)
                                            .format(formatter);

        // System.out.println("getDetailData db start");
        // System.out.println("crtpeStart"+crtpeStart);
        // System.out.println("startStr"+startStr); 
        // System.out.println("endStr"+endStr);
        // System.out.println("limit"+limit);
        // System.out.println("progStatCd"+progStatCd);
        // System.out.println("sysDivCd"+sysDivCd);
        // System.out.println("lastDate"+lastDate);

        return Mono.fromCallable(() ->
            jdbcTemplate.queryForList(sql,
                                                        // crtpeStart, endStr,
                                                        startStr, endStr,
                                                        progStatCd, progStatCd,
                                                        sysDivCd, sysDivCd,
                                                        //// crtpeStart, endStr,
                                                        crtpeStart, endStr,
                                                        // union
                                                        // crtpeStart, endStr,
                                                        startStr, endStr,
                                                        progStatCd, progStatCd,
                                                        sysDivCd, sysDivCd,
                                                        //// crtpeStart, endStr,
                                                        crtpeStart, endStr,
                                                        // top
                                                        lastDate, lastDate
                                            )
        ).subscribeOn(Schedulers.boundedElastic());
    }

    
}