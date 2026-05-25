config = {
    locateFile: filename => `js/${filename}`
}
// The `initSqlJs` function is globally provided by all of the main dist files if loaded in the browser.
// We must specify this locateFile function if we are loading a wasm file from anywhere other than the current html page's folder.
initSqlJs(config).then(function (SQL) {
    //Create the database
    const xhr = new XMLHttpRequest();
    xhr.open('GET', 'bbdd.sqlite', true);
    xhr.responseType = 'arraybuffer';
    xhr.onload = e => {
        const uInt8Array = new Uint8Array(xhr.response);
        const db = new SQL.Database(uInt8Array);
        contents = db.exec("SELECT * FROM main");
        const taula = document.querySelector('#taula');
        var tbody = taula.getElementsByTagName("tbody")[0];
        for (let i = 0; i < contents[0].columns.length; i++) {
            var valor = contents[0].columns[i];
            var col = "#col" + (i + 1);
            var el = document.querySelector(col)
            el.innerHTML = valor;
        }
        for (let i = 0; i < contents[0].values.length; i++) {
            var row = document.createElement("tr");
            valors = contents[0].values[i];
            for (let j = 0; j < valors.length; j++) {
                var cell = document.createElement("td");
                cell.innerHTML = valors[j];
                row.appendChild(cell);
            }
            tbody.appendChild(row);
        }

    };
    xhr.send();
});