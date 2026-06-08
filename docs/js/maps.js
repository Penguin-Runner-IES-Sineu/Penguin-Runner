config = {
    locateFile: filename => `js/${filename}`
}
// The `initSqlJs` function is globally provided by all of the main dist files if loaded in the browser.
// We must specify this locateFile function if we are loading a wasm file from anywhere other than the current html page's folder.
initSqlJs(config).then(function (SQL) {
    //Create the database
    const xhr = new XMLHttpRequest();
    xhr.open('GET', 'bbdd.sqlite', true);
    // xhr.open('GET', 'carModels.db', true);
    xhr.responseType = 'arraybuffer';
    xhr.onload = e => {
        const uInt8Array = new Uint8Array(xhr.response);
        const db = new SQL.Database(uInt8Array);
        contents = db.exec("SELECT * FROM mapes ");
        blocs = db.exec("SELECT mapes.id,type FROM mapes LEFT JOIN mapesBlocs ON mapes.id = MapesBlocs.idMapa LEFT JOIN blocks ON MapesBlocs.idbloc = blocks.id");
        enemies = db.exec("SELECT mapes.id,name FROM mapes LEFT JOIN MapesEnemics ON mapes.id=MapesEnemics.idMapa LEFT JOIN enemies ON MapesEnemics.idEnemic=enemies.id")

        var container = document.querySelector('.container');
        for (let i = 0; i < contents[0].values.length; i++) {
            valors = contents[0].values[i];

            nivell = document.createElement("h2")
            nivell.innerHTML = "nivell" + valors[1];

            view = document.createElement("pre")
            view.innerHTML = valors[2];

            container.append(nivell);
            container.append(view);

            var taula = document.createElement("table");
            taula.style.width = '100%';

            var headerRow = document.createElement("tr");

            var enemicsHead = document.createElement("th");
            var enemyTitle = document.createElement("h3");
            enemyTitle.innerHTML = "Tipus d'enemics";
            enemicsHead.appendChild(enemyTitle);

            var blocsHead = document.createElement("th");
            var blocsTitle = document.createElement("h3");
            blocsTitle.innerHTML = "Tipus de blocs";
            blocsHead.appendChild(blocsTitle);

            headerRow.appendChild(enemicsHead);
            headerRow.appendChild(blocsHead);
            taula.appendChild(headerRow);

            var idMapa = valors[1];
            var llistaEnemics = [];
            var llistaBlocs = [];

            for (let j = 0; j < enemies[0].values.length; j++) {
                let v = enemies[0].values[j];
                if (idMapa == v[0]) {
                    llistaEnemics.push(v[1]);
                }
            }

            for (let j = 0; j < blocs[0].values.length; j++) {
                let v = blocs[0].values[j];
                if (idMapa == v[0]) {
                    llistaBlocs.push(v[1]);
                }
            }
            var totalFiles = Math.max(llistaEnemics.length, llistaBlocs.length);

            for (let i = 0; i < totalFiles; i++) {
                var fila = document.createElement("tr");

                var tdEnemic = document.createElement("td");
                tdEnemic.innerHTML = llistaEnemics[i] !== undefined ? llistaEnemics[i] : ""; 
                fila.appendChild(tdEnemic);

                var tdBloc = document.createElement("td");
                tdBloc.innerHTML = llistaBlocs[i] !== undefined ? llistaBlocs[i] : "";
                fila.appendChild(tdBloc);

                taula.appendChild(fila);
            }

            container.appendChild(taula);

        }

    };
    xhr.send();
});