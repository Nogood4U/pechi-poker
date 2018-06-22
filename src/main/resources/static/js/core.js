const ENDPOINT = "http://localhost:8080/";

class Core {


    static createGame(game, player) {
        $.post(ENDPOINT + "game/create" + game, {player: player}).done((data) => {

        }).fail((err) => alert("could not create game."));
    }

    static listGames() {
        return $.get(ENDPOINT + "game/all").fail((err) => alert("could not create game."));
    }

    static connectToGame(game, player, listener) {
        var evtSource = new EventSource("http://localhost:8080/game/socket/" + game + "/" + player + "/stream");
        evtSource.onmessage = function (event) {
            console.log(event);
            listener(event)
        };
        evtSource.onerror = function () {
            evtSource.close();
        };
    }

}