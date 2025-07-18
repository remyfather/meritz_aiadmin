function groupByDiv(data, key) {

    const grouped = {};
    data.forEach(e => {
      if (!grouped[e.div]) grouped[e.div] = [];
      grouped[e.div].push(e);
    });
  
    return grouped;
  }
  
  
  
  function drawGpuCharts(data) {
  
    const container = document.getElementById("gpu-charts-container");
    const selectedDivs = Array.from(document.querySelectorAll(".div-filter:checked")).map(e => e.value);
  
    container.innerHTML = "";
  
    // const latestTime = [...data.gpuUtil, ...data.gpuMem]
    //   .map(e => e.time)
    //   .sort()
    //   .pop();
  
    selectedDivs.forEach(div => {
      const now = new Date();
      // const currentTimeStr = `${now.getFullYear()}-${String(now.getMonth() +1).padStart(2, '0')}-${String(now.getDate()).padStart(2, '0')} ${String(now.getHours()).padStart(2,'0')}:${String(now.getMinutes()).padStart(2, '0')}`;
      const currentHourMin = `${now.getFullYear()}-${String(now.getMonth() +1).padStart(2, '0')}-${String(now.getDate()).padStart(2, '0')} ${String(now.getHours()).padStart(2,'0')}:${String(now.getMinutes()).padStart(2, '0')}`;

      // 해당 시:분인 time 중 가장 최신 시:분:초 선택
      const times = [...data.gpuUtil, ...data.gpuMem]
      .map(e => e.time)
      .filter(t => t.startsWith(currentHourMin))
      .sort();
      const latestTime = times.pop();

      console.log("currentHourMin",currentHourMin);
      const utilList = data.gpuUtil.filter(e => e.div === div && e.time === latestTime);
      const memList = data.gpuMem.filter(e => e.div === div && e.time === latestTime);

      const util = Array(8).fill(0);
      utilList.forEach(e => { util[parseInt(e.flag)] = e.cnt; });
  
      const memStacks = {}; // { id: { GPU0: cnt, GPU1: cnt, ... } }
      memList.forEach(e => {
        const flag = parseInt(e.flag);
        const id = e.id;
        if (!memStacks[id]) memStacks[id] = Array(8).fill(0);
        memStacks[id][flag] = e.cnt;
      });
  
      const memSeries = Object.entries(memStacks).map(([id, cnts]) => ({
        name: id,
        data: cnts
      }));
  
      const chartRow = document.createElement("div");
      chartRow.className = "row mb-4";
      chartRow.innerHTML = `
        <div class="col-md-6"><div id="util-${div}" style="height: 300px;"></div></div>
        <div class="col-md-6"><div id="mem-${div}" style="height: 300px;"></div></div>
      `;
  
      container.appendChild(chartRow);
  
      Highcharts.chart(`util-${div}`, {
        chart: { type: 'column' },
        title: { text: `${div} - GPU Util (%)` },
        xAxis: { categories: [...Array(8)].map((_, i) => `GPU-${i}`) },
        yAxis: { min: 0, max: 100, title: { text: '%' } },
        plotOptions: {
            series: {
                animation: false
            }
        },
        series: [{ name: 'Util', data: util }],
      });
  
      Highcharts.chart(`mem-${div}`, {
        chart: { type: 'column' },
        title: { text: `${div} - GPU Memory (MiB)` },
        xAxis: { categories: [...Array(8)].map((_, i) => `GPU-${i}`) },
        yAxis: { min: 0, max: div === '1p'? 85000:undefined, title: { text: 'Memory (MiB)' },
                plotLines: div === '1p'? [{
                    color: 'red',
                    value: 81559,
                    width: 2,
                    dashStyle: 'Dash',
                    label: {
                        "text": "81559",
                        verticalAlign: 'top',
                        x: 0,
                        y: -10,
                        align: 'right',
                        style: {overflow:'justify',color:'red'}
                    },
                    zIndex:5
                }] : [],
        stackLabels: { enabled: true } },
        plotOptions: {
          column: { stacking: 'normal' },
          series: {
                animation: false
            }
        },
        series: memSeries
      });
  
    });
  
  }
  
  
  
  function updateDivCheckboxes(data) {
    const box = document.getElementById("div-filter-box");
  
    // 현재 체크 상태 저장
    const prevChecked = {};
    document.querySelectorAll(".div-filter").forEach(e => {
      prevChecked[e.value] = e.checked;
    });
  
    const divSet = new Set(data.gpuMem.map(e => e.div));
  
    box.innerHTML = [...divSet].map(div => {
      const isChecked = prevChecked.hasOwnProperty(div) ? prevChecked[div] : true;
      return `<div><input class="form-check-input div-filter" type="checkbox" value="${div}" ${isChecked ? 'checked' : ''}/> ${div}</div>`;
    }).join("\n");
  }
  
  function loadData() {
  
    showLoading();
  
    fetch("/api/monitor/gpu-daily-data")
      .then(res => res.json())
      .then(data => {
        updateDivCheckboxes(data);
        drawGpuCharts(data);
      })
      .catch(err => console.error("Fetch error", err))
      .finally(() => hideLoading());
  
  }
  
  document.addEventListener("DOMContentLoaded", () => {
  
    loadData();
    setInterval(loadData, 3000);
  
    document.getElementById("div-filter-box").addEventListener("change", () => {
      fetch("/api/monitor/gpu-daily-data")
        .then(res => res.json())
        .then(data => drawGpuCharts(data));
    });
  
  });