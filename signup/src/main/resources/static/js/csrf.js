(function () {
    function getCookie(name) {
        var escaped = name.replace(/[.*+?^${}()|[\]\\]/g, "\\$&");
        var match = document.cookie.match(new RegExp("(?:^|; )" + escaped + "=([^;]*)"));
        return match ? decodeURIComponent(match[1]) : "";
    }

    function isUnsafeMethod(method) {
        var m = (method || "GET").toUpperCase();
        return m !== "GET" && m !== "HEAD" && m !== "OPTIONS" && m !== "TRACE";
    }

    var nativeFetch = window.fetch.bind(window);

    window.fetch = function (input, init) {
        var options = init ? Object.assign({}, init) : {};
        var method = (options.method || "GET").toUpperCase();

        if (options.credentials === undefined) {
            options.credentials = "same-origin";
        }

        var requestUrl = typeof input === "string" ? input : (input && input.url ? input.url : "");
        var isSameOrigin = true;
        if (requestUrl) {
            try {
                var parsed = new URL(requestUrl, window.location.origin);
                isSameOrigin = parsed.origin === window.location.origin;
            } catch (e) {
                isSameOrigin = true;
            }
        }

        if (isSameOrigin && isUnsafeMethod(method)) {
            var csrfToken = getCookie("XSRF-TOKEN");
            if (csrfToken) {
                var headers = new Headers(options.headers || {});
                if (!headers.has("X-XSRF-TOKEN")) {
                    headers.set("X-XSRF-TOKEN", csrfToken);
                }
                options.headers = headers;
            }
        }

        return nativeFetch(input, options);
    };
})();
