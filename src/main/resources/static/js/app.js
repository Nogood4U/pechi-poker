class GameList extends React.Component {
    constructor(props) {
        super(props);
        Core.listGames().done((data) => {
            this.state.games = data
        });
        this.state = {
            games: [],
            player: props.player
        };
    }

    componentWillReceiveProps(nextProps) {
        if (this.props.player !== nextProps.player) {
            this.setState({player: nextProps.player, games: []});
        }
    }

    renderLi(game) {
        return <li key={game.key}>
            <div key={game.key} style={{display: "inline-flex"}}>{game.name}&nbsp;
                <JoinGame key={game.key}
                          game={game}
                          player={this.props.player}
                          setGame={this.props.setGame}/>
            </div>
        </li>
    }

    refreshGames() {
        Core.listGames().done((data) => {
            data.forEach(g => g.key = g.name);
            this.setState({games: data});
        });
    }

    printPlayerName() {
        if (this.props.player)
            return (<h1>GameList for {this.props.player}</h1>);
        else
            return "";
    }

    render() {
        return (
            <div className="shopping-list">
                {this.printPlayerName()}
                <button type="button" onClick={() => this.refreshGames()}>Actualizar</button>
                <ul>
                    {this.state.games.map((game) => this.renderLi(game))}
                </ul>
                <hr/>
                <CreateGame player={this.props.player}/>
            </div>
        );
    }
}

class CreateGame extends React.Component {

    constructor(props) {
        super(props);
        this.state = {game: "", player: props.player, msg: ""};
    }

    createGame() {
        Core.createGame(this.state.game, this.state.player).done((data) => {
            this.setState({msg: "Juego " + this.game + " creado..", game: "", player: ""});
        });
        console.log(this.state.game, this.state.player);
    }

    componentWillReceiveProps(nextProps) {
        if (this.props.player !== nextProps.player) {
            this.setState({player: nextProps.player})
        }
    }

    render() {
        return (
            <div>
                <label>Game Code:&nbsp;</label><input type="text" value={this.state.game}
                                                      onChange={event => this.setState({game: event.target.value})
                                                      }/>
                <button type="button" onClick={(evt) => this.createGame(evt)}>Create</button>
                <h3>{this.state.msg}</h3>
            </div>
        )
    }
}


class CreatePlayer extends React.Component {

    constructor(props) {
        super(props);
        this.state = {player: "", playerObj: undefined};
        this.setPlayer = props.setPlayer;
    }

    createPlayer() {
        Core.getPlayer(this.state.player)
            .done(player => {
                this.setState({playerObj: player});
                this.setPlayer(this.state.player);
            })
            .fail(err => Core.createPlayer(this.state.player)
                .done(player => {
                    this.setState({playerObj: player});
                    this.setPlayer(this.state.player);
                })
            )
    }

    render() {
        if (this.state.playerObj) {
            return (
                <div>
                    <h2>{this.state.player}</h2>
                </div>
            )
        } else
            return (
                <div>
                    <h2>Entrar como:</h2>
                    <label>Player:&nbsp;</label><input type="text"
                                                       onChange={event => this.setState({player: event.target.value})}/>
                    <button type="button" onClick={(evt) => this.createPlayer(evt)}>Join</button>
                    <h3>{this.state.msg}</h3>
                </div>
            )
    }
}


class JoinGame extends React.Component {

    constructor(props) {
        super(props);
        this.state = {game: props.game, player: props.player};
    }

    joinGame() {
        if (this.state.game && this.state.game.players.find(elm => elm.name === this.state.player)) {
            this.props.setGame(this.state.game.name);
        } else {
            Core.joinGame(this.state.game.name, this.state.player)
                .done(res => {
                    //redirect to game page or inti game
                    console.log("joining..." + this.state.game.name);
                    this.props.setGame(this.state.game.name);
                });
        }
    }

    render() {
        return (
            <div key={this.state.code}>
                <button key={this.state.code} type="button" onClick={() => this.joinGame()}>Entrar</button>
            </div>
        )
    }
}


class GameBoard extends React.Component {

    constructor(props) {
        super(props);
        this.state = {
            code: props.game, player: props.player,
            update: {playerTurn: "", tableCards: [], droppedCards: [], player: [], game_stage: ""},
            connected: false
        };
    }

    componentWillReceiveProps(nextProps) {
        if (nextProps.game !== "" && nextProps.player !== "" && !this.state.connected) {
            this.setState({code: nextProps.game, player: nextProps.player}, () => {
                this.connectGame();
            });
        }
    }

    connectGame() {
        Core.connectToGame(this.state.code, this.state.player,
            (ms) => {
                console.log(ms);
                let jsonData = $.parseJSON(ms.data);
                if (jsonData.playerTurn)
                    this.setState({update: jsonData});
            }, err => {

            });
    }

    render() {
        return (
            <div>
                <div>
                    <h3>Proximo turno:</h3>
                    {this.state.update.playerTurn}
                </div>
                <div>
                    <h3>Estado del Juego:</h3>
                    {this.state.update.game_stage}
                </div>
                <div>
                    <h3>Cartas en mesa:</h3>
                    {this.state.update.tableCards.map(card =>
                        <ul>
                            <li>{card.suits}</li>
                            <li>{card.number}</li>
                        </ul>
                    )}
                </div>
                <div>
                    <h3>Cartas descartadas:</h3>
                    {this.state.update.droppedCards.map(card =>
                        <ul>
                            <li>{card.suits}</li>
                            <li>{card.number}</li>
                        </ul>
                    )}
                </div>
                <div>
                    <h3>Jugadores:</h3>
                    {this.state.update.player.map(player =>
                        <p>
                            {player.name}
                        </p>
                    )}
                </div>
            </div>
        )
    }
}