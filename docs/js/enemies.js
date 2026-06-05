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
        contents = db.exec("SELECT * FROM enemies");
        for (let i = 0; i < contents[0].values.length; i++) {
            valors = contents[0].values[i];

            type = document.createElement("h2")
            type.innerHTML = valors[1];

            image = document.createElement("img");
            src = "media/" + valors[2];
            image.setAttribute("src", src);

            description = document.createElement("p");
            description.innerHTML = valors[3];

            document.body.append(type);
            document.body.append(image);
            document.body.append(description);
            // tbody.appendChild(row);
        }

    };
    xhr.send();
});