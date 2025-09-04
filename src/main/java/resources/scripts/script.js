function loadGetMsg() {
    let nameVar = document.getElementById("name").value;
    let ageVar = document.getElementById("age").value;
    if (!nameVar || nameVar.trim() === "") {
        nameVar = "";
    }
    if (!ageVar || ageVar.trim() == "") {
        ageVar = "";
    }
    const xhttp = new XMLHttpRequest();
    xhttp.onload = function () {
        document.getElementById("getrespmsg").innerHTML = this.responseText;
    }
    xhttp.open("GET", "/app/hello?name=" + nameVar + "&age=" + ageVar, true);
    xhttp.send();
}

function loadPostMsg(name) {
    let nameValue = name.value;
    if (!nameValue || nameValue.trim() === "") {
        nameValue = "";
    }
    
    let url = "/app/hellopost?name=" + nameValue;

    fetch(url, {method: 'POST'})
            .then(x => x.text())
            .then(y => document.getElementById("postrespmsg").innerHTML = y);
}