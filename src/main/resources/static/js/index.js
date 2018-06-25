class App extends React.Component {

    constructor(props) {
        super(props);
        this.state = {player: "", game: ""};
    }

    setPlayer(player) {
        this.setState({player: player});
    };

    setGame(game) {
        this.setState({game: game});
    };


    render() {
        return (
            <div>
                <CreatePlayer setPlayer={this.setPlayer.bind(this)}/>
                <GameList player={this.state.player} setGame={this.setGame.bind(this)}/>
                <GameBoard game={this.state.game} player={this.state.player}/>
            </div>
        )
    }

}

// Use the ReactDOM.render to show your component on the browser
ReactDOM.render(
    <App/>,
    document.getElementById(
        "root"
    )
);
