const ENDPOINT = "http://localhost:8080/";

class Core {


    static createGame(game, player) {
        return $.post(ENDPOINT + "game/create/" + game, {player: player}).fail(err => alert("could not create game."));
    }

    static startGame(game, player) {
        return $.post(ENDPOINT + "game/start/" + game, {player: player}).fail(err => alert("could not start game."));
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

    static doPlay(game, play, player, amount) {
        return $.post(ENDPOINT + "game/play/" + game + "/" + player + "/" + play, {amount: amount}).fail((err) => console.log("could not make play.."));
    }

    static call(game, player) {
        return this.doPlay(game, "call", player, 0);
    }

    static raise(game, player, amount) {
        return this.doPlay(game, "raise", player, amount);
    }

    static fold(game, play, player) {
        return this.doPlay(game, "fold", player, 0);
    }

    static pass(game, player) {
        return this.doPlay(game, "pass", player, 0);
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