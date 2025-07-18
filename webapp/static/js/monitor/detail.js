const limit = 15;
  let lastFetchedDtm = null;

  window.retry = function(pcsId, hisSeq) {
    Swal.fire({
      title: "재처리를 실행하시겠습니까?",
      icon: "question",
      showCancelButton: true,
      confirmButtonText: "예",
      cancelButtonText: "아니오"
    }).then((result) => {
      if (!result.isConfirmed) return;
  
      showLoading();
  
      fetch("/api/monitor/retry", {
        method: "POST",
        headers: {
          "Content-Type": "application/json"
        },
        body: JSON.stringify({ pcsId, hisSeq })
      })
      .then(async res => {
        const text = await res.text();
        if (!res.ok) throw new Error(text || "재처리 실패");
        return text;
      })
      .then(() => {
        Swal.fire({
          icon: "success",
          title: "재처리 완료",
          text: "재처리 요청이 성공적으로 완료되었습니다."
        }).then(() => fetchDetailData(true));
      })
      .catch(err => {
        console.error(err);
        Swal.fire({
          icon: "fail",
          title: "재처리 실패",
          html: err.message
        });
      })
      .finally(() => hideLoading());
    });
  };  

  function formatDateTime(dateStr) {
    if (!dateStr) return null;
    const date = new Date(dateStr);
    if (isNaN(date)) return dateStr; // 파싱 실패하면 원본 출력
    
    const yyyy = date.getFullYear();
    const mm = String(date.getMonth() + 1).padStart(2, '0');
    const dd = String(date.getDate()).padStart(2, '0');
    const HH = String(date.getHours()).padStart(2, '0');
    const MI = String(date.getMinutes()).padStart(2, '0');
    const SS = String(date.getSeconds()).padStart(2, '0');
    return `${yyyy}-${mm}-${dd} ${HH}:${MI}:${SS}`;
  }

  function normalizeDate(input) {
    const raw = input.value.replace(/\D/g, '');

    if(input.value.includes('-')) return;

    if (raw.length === 8) {
      const yyyy = raw.slice(0, 4);
      const mm = raw.slice(4, 6);
      const dd = raw.slice(6, 8);
      input.value = `${yyyy}-${mm}-${dd}`;
    }
  }

  function formatTime(input) {
    const raw = input.value.replace(/\D/g, '');

    if (raw.length === 6) {
      const hh = raw.slice(0, 2);
      const mm = raw.slice(2, 4);
      const ss = raw.slice(4, 6);
      input.value = `${hh}:${mm}:${ss}`;
    }
  }

  function fetchDetailData(isFirst) {

    const startDate = document.getElementById("startDate").value;
    const startTime = document.getElementById("startTime").value;
    const endDate = document.getElementById("endDate").value;
    const endTime = document.getElementById("endTime").value;
    
    if (!startDate.trim() || !startTime.trim() || !endDate.trim() || !endTime.trim()) {
      Swal.fire({
            title: '오류',
            html: '날짜와 시간을 모두 입력하세요.',
            icon: 'error',
        });
      return;
    }

    const start = `${startDate} ${startTime}`;
    const end = `${endDate} ${endTime}`;

    showLoading();

    const progStatCd = document.getElementById("progStatCd").value;
    const sysDivCd = document.getElementById("sysDivCd").value;

    let url = `/api/monitor/detail-data?start=${start}&end=${end}&limit=${limit}`;
    if(progStatCd != "A"){
      url += `&progStatCd=${encodeURIComponent(progStatCd)}`;
    }
    if(sysDivCd != "A"){
      url += `&sysDivCd=${encodeURIComponent(sysDivCd)}`;
    }

    if(isFirst){
      lastFetchedDtm = null;
    } else {
      url += `&lastDate=${encodeURIComponent(formatDateTime(lastFetchedDtm))}`;
    }

    fetch(url)
      .then(res => res.json())
      .then(data => {
        const tbody = document.getElementById("resultTable");
        if(isFirst) tbody.innerHTML = "";

        if(!data || !data.dataList || data.dataList.length === 0){
          if(isFirst){
            tbody.innerHTML = `<tr><td colspan="15" class="text-center">데이터가 없습니다</td></tr>`;
          }
          document.getElementById("moreButton").style.display = "none";
          return;
        }

        data.dataList.forEach(row => {
          const tr = document.createElement("tr");
          tr.innerHTML = `
            <td>${row.REC_ID ?? '-'}</td>
            <td>${row.RECORD_DURATION ?? '-'}</td>
            <td>${formatDateTime(row.CNVR_ST_DTM)}</td>
            <td>${formatDateTime(row.STT_ST_DTM)}</td>
            <td>${formatDateTime(row.STT_ED_DTM)}</td>
            <td>${row.STT_TOTAL_DURATION ?? '-'}</td>
            <td>${row.STT_DURATION ?? '-'}</td>
            <td>${formatDateTime(row.PCS_ST_DTM)}</td>
            <td>${formatDateTime(row.PCS_ED_DTM)}</td>
            <td>${row.SMRY_DURATION ?? '-'}</td>
            <td>${row.PROG_STAT_CD ?? '-'}</td>
            <td>${row.RSLT_STAT_MSG ?? '-'}</td>
            <td>${row.WGHT_CD ?? '-'}</td>
            <td>${row.HS ?? '-'}</td>
            <td>${row.TOTAL_EXCUTION_TIME ?? '-'}</td>
            <td>
              ${row.RETRY_ENABLE_YN === 'Y' && row.PROG_STAT_CD === 'E'
              ? `<button onclick="retry('${row.REC_ID}', ${row.HS})">재처리</button>`
              : '-'}
            </td>
          `;
          tbody.appendChild(tr);
        });

        if(data.dataList.length > 0){
          lastFetchedDtm = data.dataList[data.dataList.length - 1].PCS_ST_DTM;
        }
        document.getElementById("moreButton").style.display = "block";
      })
      .catch(err => {
        console.error(err);
        // alert("조회 실패");
        Swal.fire({
            title: '알림',
            html: '조회 실패',
            icon: 'error',
        });
      })
      .finally(() => hideLoading());
      ;

  }

  function openDownloadPopup() {
    document.getElementById("downloadModal").style.display = "block";

  }

  function closeDownloadPopup() {
    document.getElementById("downloadModal").style.display = "none";
  }

  function downloadExcel() {
    const date = document.getElementById("downloadDate").value;
    if (!date) {
      // alert("날짜를 입력하세요");
      Swal.fire({
          title: '조회 실패',
          html: '날짜를 입력하세요.',
          icon: 'error',
      });
      return;
    }

    showLoading();

    fetch(`/api/monitor/detail/check-data?date=${date}`)
    .then(res => {
      if(!res.ok) throw new Error("엑셀 생성 불가");
      return res.json();
    })
    .then(data => {
      if(data.count == 0){
        // alert("해당 날짜에 다운로드할 데이터가 없습니다.");
        Swal.fire({
            title: '다운로드 실패',
            html: '해당 날짜에 다운로드할 데이터가 없습니다.',
            icon: 'error',
        });
        hideLoading();
        return;
      }

      // 다운로드 진행
      window.open(`/api/monitor/detail/excel?date=${date}`, "_blank");
      hideLoading();
      closeDownloadPopup();
    })
    .catch(err => {
      // alert("오류 발생: "+err.message);
      Swal.fire({
          title: '오류 발생',
          html: err.message,
          icon: 'error',
      });
      hideLoading();
    });
  }

  window.onload = function () {
    const now = new Date();
    const yyyy = now.getFullYear();
    const MM = String(now.getMonth() + 1).padStart(2, '0');
    const dd = String(now.getDate()).padStart(2, '0');
    const HH = String(now.getHours()).padStart(2, '0');
    const mm = String(now.getMinutes()).padStart(2, '0');

    const todayStr = `${yyyy}-${MM}-${dd}`;
    document.getElementById("downloadDate").value = todayStr;
    document.getElementById("startDate").value = `${yyyy}-${MM}-${dd}`;
    document.getElementById("startTime").value = `00:00:00`;
    document.getElementById("endDate").value = `${yyyy}-${MM}-${dd}`;
    document.getElementById("endTime").value = `23:59:59`;

    const startDateInput = document.getElementById("startDate");
    const endDateInput = document.getElementById("endDate");
    const startTimeInput = document.getElementById("startTime");
    const endTimeInput = document.getElementById("endTime");

    // date 기준
    let prevStartDate = startDateInput.value;

    startDateInput.addEventListener("change", () => {
      const startDate = startDateInput.value;
      if(startDate && startDate !== prevStartDate){
        endDateInput.value = startDate;
        prevStartDate = startDate;
      }
    });

    endDateInput.addEventListener("change", () => {
      const endDate = endDateInput.value;
      const startDate = startDateInput.value;
      if(endDate && startDate && endDate !== prevStartDate){
        startDateInput.value = endDate;
        prevStartDate = endDate;
      }
    });

    startTimeInput.addEventListener("change", () => {
      const endTime = endTimeInput.value;
      const startTime = startTimeInput.value;
      if(endTime && startTime && endTime < startTime){
        endDateInput.value = "23:59:59";
      }
    });

    /*
    // text 기준
    const startDateInput = document.getElementById("startDate");
    const endDateInput = document.getElementById("endDate");
    const startTimeInput = document.getElementById("startTime");
    const endTimeInput = document.getElementById("endTime");

    let prevStartDate = "";

    startDateInput.addEventListener("input", () => {
      const startDate = startDateInput.value;
      if(startDate && startDate !== prevStartDate){
        endDateInput.value = startDate;
        prevStartDate = startDate;
      }
    });

    endDateInput.addEventListener("input", () => {
      const endDate = endDateInput.value;
      const startDate = startDateInput.value;
      if(endDate && startDate && endDate !== prevStartDate){
        startDateInput.value = endDate;
        prevStartDate = endDate;
      }
    });

    startTimeInput.addEventListener("input", () => {
      const endTime = endTimeInput.value;
      const startTime = document.getElementById("startTime").value;
      if(endTime && startTime && endTime < startTime){
        endDateInput.value = "23:59:59";
      }
    });
    */

    
  };