let initialStart = null;
let initialEnd = null;

function initDateInputs() {
    const today = new Date();
    const start = new Date(today.getTime() - 3 * 24 * 60 * 60 * 1000);
    document.getElementById("startDate").valueAsDate = start;
    document.getElementById("endDate").valueAsDate = today;
}

function getFormattedDate(inputId) {
    const val = document.getElementById(inputId).value;
    return val.replace(/-/g, '');
}

function getUnitByRange(ms) {
    const day = 86400000, hour = 3600000, min = 60000;
    // if (ms <= min * 60) return 'second';
    if (ms <= hour * 24) return 'minute';
    if (ms <= day * 14) return 'hour';
    return 'day';
}

function getTooltipFormat(unit) {
    switch (unit) {
        case 'day': return '%Y-%m-%d';
        case 'hour': return '%Y-%m-%d %H';
        case 'minute': return '%Y-%m-%d %H:%M';
        case 'second': return '%Y-%m-%d %H:%M:%S';
        default: return '%Y-%m-%d %H:%M';
    }
}

function parseDateTimeToMillis(datetimeStr) {
    const [datePart, timePart = "00:00"] = datetimeStr.split(" ");
    const [yyyy, MM, dd] = datePart.split("-");
    const [hh = "00", mm = "00"] = timePart.split(":");
    return Date.UTC(+yyyy, +MM - 1, +dd, +hh, +mm);
}

function groupSeries(data, labelPrefix = "") {
    const map = {};
    for (const row of data) {
        const ts = parseDateTimeToMillis(row.time);
        const label = labelPrefix + row.flag;
        if (!map[label]) map[label] = { name: label, data: [] };
        map[label].data.push([ts, row.cnt]);
    }
    return Object.values(map);
}

function drawAllCharts(data, unit) {
    const tooltipFormat = getTooltipFormat(unit);
    const chartBase = {
        chart: {
            zoomType: 'x',
            type: 'spline',
            events: {
                selection: function (event) {
                    const min = event.xAxis[0].min;
                    const max = event.xAxis[0].max;
                    const unit = getUnitByRange(max - min);
                    const start = Highcharts.dateFormat('%Y%m%d%H%M%S', min);
                    const end = Highcharts.dateFormat('%Y%m%d%H%M%S', max);
                    fetch(`/api/monitor/period-data?start=${start}&end=${end}&unit=${unit}`)
                        .then(res => res.json())
                        .then(d => drawAllCharts(d, unit));
                    return false;
                }
            }
        },
        xAxis: {
            type: 'datetime',
            title: { text: '시간' }
        },
        yAxis: {
            title: { text: '건수' }
        },
        tooltip: {
            shared: true,
            formatter: function () {
                const format = getTooltipFormat(unit);
                const time = Highcharts.dateFormat(format, this.x);

                if (this.points && this.points.length > 0) {
                    const lines = this.points.map(function (p) {
                        return `<span style="color:${p.color}">\u25CF</span> ${p.series.name}: <b>${p.y}</b>`;
                    });
                    return `<b>${time}</b><br/>` + lines.join('<br/>');
                } else {
                    return `<b>${time}</b>`;
                }
            }
        }
    };

    Highcharts.chart('flag-s-chart', Object.assign({}, chartBase, {
        title: { text: 'STT 처리중 건수' },
        series: groupSeries(data.flags || [])
    }));

    Highcharts.chart('flag-S-chart', Object.assign({}, chartBase, {
        title: { text: '데몬2 처리중 건수' },
        series: groupSeries(data.flagS || [])
    }));

    /*
    Highcharts.chart('gpu-chart', Object.assign({}, chartBase, {
        title: { text: 'GPU 사용률 추이' },
        yAxis: { min: 0, max: 100, title: { text: '사용률 (%)' } },
        series: groupSeries(data.gpu || [], 'GPU-')
    }));
    */

    Highcharts.chart('error-chart', Object.assign({}, chartBase, {
        title: { text: '에러 발생 건수' },
        series: groupSeries(data.errorGrouped || [])
    }));
}

function loadPeriodData() {
    const start = getFormattedDate("startDate") + "000000";
    const end = getFormattedDate("endDate") + "235959";
    initialStart = start;
    initialEnd = end;

    showLoading();

    fetch(`/api/monitor/period-data?start=${start}&end=${end}&unit=day`)
        .then(res => res.json())
        .then(data => drawAllCharts(data, 'day'))
        .catch(err => console.error(err))
        .finally(() => hideLoading());
    ;
}

function resetToInitial() {
    if (!initialStart || !initialEnd) return;

    showLoading();

    fetch(`/api/monitor/period-data?start=${initialStart}&end=${initialEnd}&unit=day`)
        .then(res => res.json())
        .then(data => {
            drawAllCharts(data, 'day');

            // 초기화 (기존 확대 제거)
            // ['flag-s-chart', 'flag-S-chart', 'gpu-chart', 'error-chart'].forEach(id => {
            ['flag-s-chart', 'flag-S-chart', 'error-chart'].forEach(id => {
                const chart = Highcharts.charts.find(c => c && c.renderTo.id === id);
                if (chart) {
                    chart.xAxis[0].setExtremes(null, null, true, false);
                }
            });
        })
        .catch(err => console.error(err))
        .finally(() => hideLoading());
    ;
}

window.addEventListener('DOMContentLoaded', () => {
    initDateInputs();
    loadPeriodData();
});