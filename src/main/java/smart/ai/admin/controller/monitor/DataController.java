package smart.ai.admin.controller.monitor;

import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import reactor.core.publisher.Mono;
import smart.ai.admin.service.monitor.MonitorService;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;


@RestController
@RequestMapping("/api/monitor")
public class DataController {

    private final MonitorService monitorService;

    private static final Logger log = LoggerFactory.getLogger(DataController.class);

    public DataController(MonitorService monitorService) {
        this.monitorService = monitorService;
    }

    /**
     * 재처리
     */
    @PostMapping("/retry")
    public Mono<ResponseEntity<?>> retry(@RequestBody Map<String, Object> body) {
        String pcsId = (String) body.get("pcsId");
        Integer hisSeq = (Integer) body.get("hisSeq");
    
        if (pcsId == null || hisSeq == null) {
            return Mono.just(ResponseEntity.badRequest().body("pcsId, hisSeq 모두 필요"));
        }
    
        return monitorService.retry(pcsId, hisSeq)
                .doOnNext(result -> System.out.println("retry result: "+ result))
                .map(result -> result
                        ? ResponseEntity.ok().build()
                        : ResponseEntity.status(500).body("재처리 실패")
                )
                .onErrorResume(e -> {
                    e.printStackTrace();
                    return Mono.just(ResponseEntity.status(500).body("서버 에러: "+e.getMessage()));
                });
    }

    /**
     * agg 기준 peak date 조회
     */
    @GetMapping("/peak-date")
    public Mono<Map<String,String>> getPeakDate(
        @RequestParam String div,
        @RequestParam String unit,
        @RequestParam String aggType
    ) {
        
        return monitorService.getPeakDate(div, unit, aggType)
                .map(date -> Map.of("peakDate", date));
    }
    

    /**
     * 실시간 GPU 조회
     */
    @GetMapping("/gpu-daily-data")
    public Mono<Map<String, List<Map<String, Object>>>> getGpuDailyChart() {
        return monitorService.getGpuDailyChart();
    }

    /**
     * 당일 차트 조회 (일일차트 메뉴)
     */
    @GetMapping("/daily-data")
    public Mono<Map<String, List<Map<String, Object>>>> getDailyChart() {
        return monitorService.getDailyChart(); // flagS, flagC, errorGrouped 포함
    }

    /**
     * 기간 내 차트 조회 (기간별차트 메뉴)
     */
    @GetMapping("/period-data")
    public Mono<Map<String, List<Map<String, Object>>>> getPeriodChart(
            @RequestParam String start,
            @RequestParam String end,
            @RequestParam String div,
            @RequestParam String unit,
            @RequestParam String aggType
        ) {
        return monitorService.getPeriodChart(start, end, div, unit, aggType); // flagS, flagC, errorGrouped 포함
    }

    /**
     * 기간 내 페이징 조회 (상세조회 메뉴)
     */
    @GetMapping("/detail-data")
    public Mono<Map<String, Object>> getDetail(
            @RequestParam String start,
            @RequestParam String end,
            @RequestParam int limit,
            @RequestParam(required = false) String progStatCd,
            @RequestParam(required = false) String sysDivCd,
            @RequestParam(required = false) String lastDate
    ) {
        if("A".equals(progStatCd)){
            progStatCd = null;
        }

        if("A".equals(sysDivCd)){
            sysDivCd = null;
        }

        return monitorService.getDetailData(start, end, limit, progStatCd, sysDivCd, lastDate);
        
        // Mono<Map<String,Object>> result;
        // try{
        //     result = monitorService.getDetailData(start, end, limit, progStatCd, sysDivCd, lastDate);
        //     System.out.println("result finish");
        // }catch (Exception e){
        //     e.printStackTrace();
        //     return Mono.error(e);
        // }
        // System.out.println("result"+result);
        // System.out.println("getDetailData end");

        // return result.doOnSubscribe(sub->System.out.println("?"))
        // .doOnSubscribe(map->System.out.println("return data"+map))
        // .doOnError(Throwable::printStackTrace);
    }

    @GetMapping("/detail/excel")
    public Mono<ResponseEntity<byte[]>> downloadExcel(@RequestParam String date) {
        return monitorService.generateExcel(date);
    }

    @GetMapping("/detail/check-data")
    public Mono<Map<String, Object>> checkData(@RequestParam String date) {
        return monitorService.countRowsByDate(date)
                            .map(count -> Map.of("count", count));
    }
}
