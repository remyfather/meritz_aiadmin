document.addEventListener('DOMContentLoaded', function () {
    const texts = [...document.getElementsByClassName('process-info') || []].map(item => item?.children?.[0]?.id?.split('-').slice(1));
    const req = texts.reduce((acc, cur) => {
        if (cur === undefined) return;
        const [key, ...rest] = cur;
        acc[key] = acc[key] ?? [];
        acc[key].push(rest);
        return acc
    }, {})
    if (req === undefined) return;
    getStatus(req).then(() => {
        setInterval(async () => { await getStatus(req) }, 5000);
    });
    getThreadCnt(req);
})

async function getThreadCnt(req) {
    const filtered = Object.fromEntries(
        Object.entries(req).map(([k, v]) => [
            k,
            v.filter(([_, num]) => num === '2')
        ])
    );
    return Fetcher('/api/admin/thread-cnt', {
        method: 'POST',
        body: JSON.stringify(filtered),
        onStream: stream => {
            const { status, host, target, num, body } = stream;
            if (status === 200) {
                setThreadValue(host, target, num, body);
            } else {
                setThreadValue(host, target, num, 0);
            }
        }
    });
}

async function getStatus(req) {
    return Fetcher('/api/admin/status', {
        method: 'POST',
        body: JSON.stringify(req),
        onStream: stream => {
            const { status, host, target, num } = stream;
            if (status === 200) {
                setStatus(host, target, num, 'online');
            } else {
                setStatus(host, target, num, 'offline');
            }
        }
    });
}

async function setStatus(host, target, num, status) {
    const element = document.getElementById(`status-${host}-${target}-${num}`);
    element.classList.remove('online', 'offline', 'stanby');
    element.classList.add(status);
}

async function setThreadValue(host, target, num, value) {
    const element = document.getElementById(`input-${host}-${target}-${num}`);
    element.value = value;
}

async function startService(host, target, num) {
    const check = await Swal.fire({
        title: `${host} ${target}_daemon${num}`,
        html: '실행하시겠습니까?',
        icon: 'warning',
        showCancelButton: true
    });

    if (!check.isConfirmed) return;
    const param = {
        hostName: host,
        target: target,
        num: num
    }
    streamRequest('/api/admin/start', param).then(() => {
        setStatus(host, target, num, 'online');
    }).catch(e => { });
}

async function stopService(host, target, num) {
    const check = await Swal.fire({
        title: `${host} ${target}_daemon${num}`,
        html: '중지하시겠습니까?',
        icon: 'warning',
        showCancelButton: true
    });

    if (!check.isConfirmed) return;
    const param = {
        hostName: host,
        target: target,
        num: num
    }
    return streamRequest('/api/admin/stop', param).then(() => {
        setStatus(host, target, num, 'offline');
    }).catch(e => { });
}

async function updateService(host, target, num) {
    const check = await Swal.fire({
        title: `${host} ${target}_daemon${num}`,
        html: '적용하시겠습니까?<br>적용시 해당 데몬이 재기동됩니다.<br><br>적용 전 모델 Replica 수를 확인하세요!',
        icon: 'warning',
        showCancelButton: true
    });

    if (!check.isConfirmed) return;
    const input = document.getElementById(`input-${host}-${target}-${num}`);
    if (!input) return;
    const param = {
        hostName: host,
        target: target,
        num: num,
        cnt: input.value,
    }
    return streamRequest('/api/admin/update', param).then(() => {
        setStatus(host, target, num, 'online');
    }).catch(e => { });
}

async function streamRequest(api, param) {
    const buttonArea = document.getElementById(`${param.hostName}-${param.target}-${param.num}`);
    const modalText = document.getElementById('modalContent');
    buttonArea.classList.add('requested');
    showLoading();
    openModal();
    return Fetcher(api, {
        method: 'POST',
        body: JSON.stringify(param),
        onStream: stream => {
            const { status, body } = stream;
            if (status === 200) {
                modalText.innerHTML += body + '<br>';
            } else {
                throw new Error('Internal Server Error');
            }
        }
    }).catch(e => {
        modalText.innerHTML += '-------------------------' + '<br>';
        modalText.innerHTML += '실행에 실패했습니다.' + '<br>';
        throw e;
    }).finally(() => {
        buttonArea.classList.remove('requested');
        hideLoading();
    })
}

function openModal() {
    const dataModal = new bootstrap.Modal(document.getElementById('dataModal'))
    const modalText = document.getElementById('modalContent');
    modalText.innerHTML = '';
    dataModal.show();
}