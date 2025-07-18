let isHovering = false;
const groupKeys = {
  TM: ['flagsT', 'flagST', 'flagET'],
  CALL: ['flagsC', 'flagSC', 'flagEC']
};

const chartTitles = {
  flagsT: 'STT 처리중',
  flagST: '요약 처리중',
  flagET: '요약 오류',
  flagsC: 'STT 요청중',
  flagSC: '요약 처리중',
  flagEC: '요약 오류'
};

const chartMap = {};

async function fetchData(start, end, aggType, div) {
  const res = await fetch(`/api/monitor/period-data?start=${start}&end=${end}&unit=minute&aggType=${aggType}&div=${div}`);
  if (!res.ok) throw new Error('조회 실패');
  return await res.json();
}

function makeTimeLabels() {
  const labels = [];
  for (let i = 0; i < 1440; i++) {
    const h = String(Math.floor(i / 60)).padStart(2, '0');
    const m = String(i % 60).padStart(2, '0');
    labels.push(`${h}:${m}`);
  }
  return labels;
}

function normalizeSeries(raw, key, isToday = false) {
  const map = new Map();
  (raw[key] || []).forEach(e => {
    const time = e.time.replace(/\u00A0/g, ' ').slice(-5); // 'HH:MM'
    map.set(time, e.cnt);
  });

  const now = new Date();
  const nowMinutes = now.getHours() * 60 + now.getMinutes();

  return makeTimeLabels().map((label, i) => {
    if (isToday && i > nowMinutes) return null;
    return map.get(label) ?? 0;
  });
}

function getSeriesVisibility(chartKey) {
  try {
    return JSON.parse(localStorage.getItem(`chartVisibility_${chartKey}`)) || {};
  } catch (e) {
    return {};
  }
}

 

function saveSeriesVisibility(chartKey, chart) {

  const visibilities = {};
  chart.series.forEach(s => {
    visibilities[s.name] = s.visible;
  });
  localStorage.setItem(`chartVisibility_${chartKey}`, JSON.stringify(visibilities));

}


function drawGroupCharts(group, todayRaw, peakRaw) {

  const allTimes = makeTimeLabels();

 

  groupKeys[group].forEach(key => {

    const container = `${key}-chart`;

    const visibility = getSeriesVisibility(container);

 

    const series = [

      {

        name: `today_${key}`,  // �� 이름에 key 붙임

        data: normalizeSeries(todayRaw, key, true),

        type: 'line',

        visible: visibility[`today_${key}`] !== false

      },

      {

        name: `peakDate_${key}`,  // �� 이름에 key 붙임

        data: normalizeSeries(peakRaw, key, false),

        type: 'area',

        color: 'rgba(136,136,136,0.7)',

        fillOpacity: 0.7,

        lineWidth: 0,

        visible: visibility[`peakDate_${key}`] !== false

      }

    ];

 

    const chart = Highcharts.chart(container, {

      chart: {

        type: 'line',

        zoomType: null

      },

      title: { text: `${group} - ${chartTitles[key] ?? key}` },

      xAxis: {

        categories: allTimes,

        tickInterval: 180,

        labels: {

          rotation: 0,

          autoRotation: [],

          overflow: 'justify',

          style: {

            fontSize: '10px',

            whiteSpace: 'nowrap'

          }

        }

      },

      yAxis: {

        title: { text: '건수' },

        labels: {

          style: { fontSize: '10px' }

        }

      },

      tooltip: { shared: true },

      legend: {

        enabled: true,

        align: 'center',

        verticalAlign: 'bottom'

      },

      plotOptions: {

        series: {

          events: {

            legendItemClick: function () {

              const chart = this.chart;

              setTimeout(() => {

                saveSeriesVisibility(container, chart);

              }, 0);

            }

          }

        }

      },

      series: series

    });

 

    chartMap[container] = chart;

  });

}

async function loadAndDrawAll(aggType, peakDateT, peakDateC, withLoading = false) {
  if (withLoading) showLoading();

  try {
    const now = new Date();
    const yyyy = now.getFullYear();
    const MM = String(now.getMonth() + 1).padStart(2, '0');
    const dd = String(now.getDate()).padStart(2, '0');
    const HH = String(now.getHours()).padStart(2, '0');
    const mm = String(now.getMinutes()).padStart(2, '0');
    const ss = String(now.getSeconds()).padStart(2, '0');
    
    const nowStr = `${yyyy}${MM}${dd}${HH}${mm}${ss}`;1
        const todayStr = now.toISOString().slice(0, 10).replace(/-/g, '') + '000000';

    const peakTStart = peakDateT.replace(/-/g, '') + '000000';
    const peakTEnd   = peakDateT.replace(/-/g, '') + '235959';
    const peakCStart = peakDateC.replace(/-/g, '') + '000000';
    const peakCEnd   = peakDateC.replace(/-/g, '') + '235959';

    const [tmPeak, tmToday, callPeak, callToday] = await Promise.all([
      fetchData(peakTStart, peakTEnd, aggType, 'T'),
      fetchData(todayStr, nowStr, aggType, 'T'),
      fetchData(peakCStart, peakCEnd, aggType, 'C'),
      fetchData(todayStr, nowStr, aggType, 'C')
    ]);

    drawGroupCharts('TM', tmToday, tmPeak);
    drawGroupCharts('CALL', callToday, callPeak);
  } catch (e) {
    console.error('조회 실패:', e);
  } finally {
    if (withLoading) hideLoading();
  }
}

document.addEventListener('mouseover', () => { isHovering = true; });
document.addEventListener('mouseout', () => { isHovering = false; });
document.addEventListener('DOMContentLoaded', () => {
  const aggTypeSelect = document.getElementById('aggType');
  let currentAgg = aggTypeSelect.value;

  function fetchPeakDates() {
    return Promise.all([
      fetch(`/api/monitor/peak-date?unit=day&div=T&aggType=${currentAgg}`).then(r => r.json()),
      fetch(`/api/monitor/peak-date?unit=day&div=C&aggType=${currentAgg}`).then(r => r.json())
    ]);
  }

  fetchPeakDates().then(([peakT, peakC]) => {
      loadAndDrawAll(currentAgg, peakT.peakDate.trim(), peakC.peakDate.trim(), true);
  });

  aggTypeSelect.addEventListener('change', () => {
    currentAgg = aggTypeSelect.value;
    fetchPeakDates().then(([peakT, peakC]) => {
      loadAndDrawAll(currentAgg, peakT.peakDate.trim(), peakC.peakDate.trim(), true);
    });
  });

  setInterval(() => {
    if(isHovering) return;

    fetchPeakDates().then(([peakT, peakC]) => {
      loadAndDrawAll(currentAgg, peakT.peakDate.trim(), peakC.peakDate.trim(), false);
    });
  }, 3000);
});
