async function Fetcher(url, options={}) {
    try {
        const { onStream, ...fetchOptions } = options;
        const res = await fetch(url, fetchOptions);
        if (!res.ok) {
            throw new Error(`HTTP Error: ${res.status} ${res.statusText}`);
        }
        if (onStream) {
            const reader = res.body.getReader();
            const decoder = new TextDecoder('utf-8');
            while (true) {
                const { done, value } = await reader.read();
                if (done) break;
                const chunk = decoder.decode(value, { stream: true });
                const parts = chunk.split('\n\n');
                for (const part of parts) {
                    if (part === '') continue;
                    try {
                        onStream(JSON.parse(part.substring('data:'.length)));
                    } catch (e) { throw e; }
                }
            }
        } else {
            return res;
        }
    } catch (e) {
        throw e;
    }
}
