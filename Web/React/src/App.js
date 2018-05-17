import React, { Component } from 'react';
import { Grid } from 'semantic-ui-react'
import lab9k from './images/lab9k.png';
import digipolis from './images/digipolis.png';
import dekrook from './images/dekrook.jpg';

import './App.css';

import { DB_CONFIG } from './config/config';
import firebase from 'firebase/app'
import 'firebase/database';

import { Map } from './components/Map';
import { List } from './components/List';

class App extends Component {

  constructor(){
    super();
    this.state = {
      device: null
    }
    this.db = firebase.initializeApp(DB_CONFIG);
  }

  componentDidMount(){
    document.title = "Bibliocation"
  }

  changeDevice(device){
    this.setState({
      device: device
    })
  }
  render() {
    return (
      <div>       
        <div className="ui large fixed blue inverted borderless menu">
          <div className="ui container">
            <a className="item">Bibliocation</a>
          </div>
        </div>
        <div className="ui container content"> 
        <Grid>
          <Grid.Row>
            <Grid.Column width={3} only='tablet computer'>
              <List handleChangeDevice={this.changeDevice.bind(this)} firebase={this.db}></List>
            </Grid.Column>
            <Grid.Column width={16} only='mobile'>
              <List handleChangeDevice={this.changeDevice.bind(this)} firebase={this.db} dropdown={true}></List>
            </Grid.Column>
            <Grid.Column width={13} only='tablet computer'>
              <Map device={this.state.device} firebase={this.db}></Map>
              <div className='imageGrid'>
              <a rel="noopener noreferrer" href='https://lab9k.github.io' target='_blank'><img src={lab9k} alt='lab9k'/></a>
              <a rel="noopener noreferrer" href='https://www.digipolis.be' target='_blank'><img src={digipolis} alt='lab9k'/></a>
              <a rel="noopener noreferrer" href='http://dekrook.be' target='_blank'><img src={dekrook} alt='lab9k'/></a>
              </div>
            </Grid.Column>
          </Grid.Row>
          <Grid.Row>
          <Grid.Column width={16} only='mobile'>
              <Map device={this.state.device} firebase={this.db}></Map>
              <div className='imageGrid'>
              <a rel="noopener noreferrer" href='https://lab9k.github.io' target='_blank'><img src={lab9k} alt='lab9k'/></a>
              <a rel="noopener noreferrer" href='https://www.digipolis.be' target='_blank'><img src={digipolis} alt='lab9k'/></a>
              <a rel="noopener noreferrer" href='http://dekrook.be' target='_blank'><img src={dekrook} alt='lab9k'/></a>
              </div>
            </Grid.Column>
          </Grid.Row>
        </Grid> 
        </div>
      </div>
    );
  }
}

export default App;
