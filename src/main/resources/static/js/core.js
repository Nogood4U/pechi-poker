const ENDPOINT = "http://localhost:8080/";

class Core {


    static createGame(game, player) {
        return $.post(ENDPOINT + "game/create/" + game, {player: player}).fail(err => alert("could not create game."));
    }

    static listGames() {
        return $.get(ENDPOINT + "game/all").fail((err) => console.log("could not create game."));
    }

    static joinGame(code, player) {
        return $.get(ENDPOINT + "game/join/" + code, {player: player}).fail((err) => console.log("could not create game."));
    }

    static getPlayer(player) {
        return $.get(ENDPOINT + "player/" + player).fail((err) => console.log("could not get player.."));
    }

    static createPlayer(player) {
        return $.post(ENDPOINT + "player/create", {name: player}).fail((err) => console.log("could not create player.."));
    }

    static connectToGame(game, player, msgListener, errListener) {
        var evtSource = new EventSource("http://localhost:8080/game/socket/" + game + "/" + player + "/stream");
        evtSource.onmessage = function (event) {
            console.log(event);
            msgListener(event)
        };
        evtSource.onerror = function (err) {
            errListener(err);
            evtSource.close();
        };
    }

}