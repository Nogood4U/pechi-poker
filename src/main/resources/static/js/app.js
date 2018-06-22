class GameList extends React.Component {
    constructor(props) {
        super(props);
        Core.listGames().done((data) => {
            this.state.games = data
        });
        this.state = {
            games: []
        };
    }

    static renderLi(game) {
        return <li>game.code</li>
    }

    static refreshGames() {
        Core.listGames().done((data) => {
            this.state.games = data
        });
    }

    render() {
        return (
            <div className="shopping-list">
                <h1>GameList for {this.props.name}</h1>
                <button type="button" onClick={this.refreshGames}>Actualizar</button>
                <ul>
                    {this.state.games.map((game) => GameList.renderLi(game))}
                </ul>
                <hr/>
                <CreateGame/>
            </div>
        );
    }
}

class CreateGame extends React.Component {

    constructor(props) {
        super(props);
        this.state = {game: "asd", player: "312"};
    }

    createGame() {
        // Core.createGame(this.game, this.player)
        console.log(this.state.game, this.state.player);
    }

    render() {
        return (
            <div>
                <label>Game Code</label><input type="text" value={this.state.game}
                                               onChange={(event) => {
                                                   debugger;
                                                   return this.setState({
                                                       game: event.target.value,
                                                       player: this.state.player
                                                   });
                                               }}/>
                <label>Player</label><input type="text" value={this.state.player}
                                            onChange={(event) => this.setState({
                                                player: event.target.value,
                                                game: this.state.player
                                            })}/>
                <button type="button" onClick={(evt) => this.createGame(evt)}>Create</button>
            </div>
        )
    }
}