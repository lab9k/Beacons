import React from 'react';
import verdiep_0 from '../images/verdiep_0.png';
import verdiep_min1 from '../images/verdiep_min_1.png';
import verdiep_min2 from '../images/verdiep_min_2.png';
import verdiep_1 from '../images/verdiep_1.png';
import verdiep_2 from '../images/verdiep_2.png';
import verdiep_3 from '../images/verdiep_3.png';
import beacons from '../data/beacons.json';
import { Dimmer } from 'semantic-ui-react'

export class Beacon {
    constructor(id, x,  y){
        // eslint-disable-next-line 
        this.id = id,
        this.x = x,
        this.y = y, 
        this.rssi = Number.MIN_SAFE_INTEGER
    }
}

export class Map extends React.Component {

    // normal size image : 1125 x 520
    constructor(props){
        super(props);
        this.device = null;
        this.beacons = beacons.map((beacon) => new Beacon(beacon.id, beacon.x, beacon.y))
        this.state = {
            img: verdiep_0,
            verdiep_height: 0,
            verdiep_width: 0,
            active: true,
            scale_x: 0,
            scale_y: 0,
            closestBeacons: [],
            verdiep: 'Verdiep0'
        };
        this.configCanvas = this.configCanvas.bind(this);
        this.drawBeacon = this.drawBeacon.bind(this);
        this.drawBeacons = this.drawBeacons.bind(this);
        this.calcClosestBeacon = this.calcClosestBeacon.bind(this);
    }

    componentDidMount() {
        window.addEventListener('resize', this.handleOnLoadAndResize.bind(this));
        this.configCanvas();
    }

    handleOnLoadAndResize() {
        this.setState({
            verdiep_height: this.refs.verdiep.clientHeight,
            verdiep_width: this.refs.verdiep.clientWidth,
            scale_x: this.refs.verdiep.clientWidth / 1125,
            scale_y: this.refs.verdiep.clientHeight / 520
        }, () => {
            this.configCanvas();
            this.calcClosestBeacon();
        }
        ); 
    }
    
    configCanvas(){
        var wrapper = this.refs.wrapper;
        var img = this.refs.verdiep
        var canvas = this.refs.canvas;

        wrapper.width = img.clientWidth;
        wrapper.height = img.clientHeight;
        wrapper.style.position = "absolute";
        wrapper.style.left = img.offsetLeft + "px";
        wrapper.style.top = img.offsetTop + "px"; 

        canvas.width = img.clientWidth;
        canvas.height = img.clientHeight;
        canvas.style.position = "absolute";
        canvas.style.left = img.offsetLeft + "px";
        canvas.style.top = img.offsetTop + "px"; 

    }
    /* compares 2 beacons with the rssi signal */ 
    compare(a, b){
        if(a.rssi < b.rssi){
            return 1
        }
        if(a.rssi > b.rssi){
            return -1
        }
        return 0
    }

    drawBeacons(){
        for(let {id,x,y,rssi} of this.beacons){
            var size, color;
            this.state.scale_x < 1 ? size = 2 : size = 3
            Number(rssi) > Number.MIN_SAFE_INTEGER ? color = "ORANGE" : color = "RED"
            var cBeacon = this.state.closestBeacons.find(b => b.id === id)
            if(cBeacon){
                color = "GREEN"
            }
            if(this.state.verdiep === "Verdiep0" && id >= 61 && id <= 113){
                this.drawBeacon(x * this.state.scale_x, y * this.state.scale_y, size, color)
            }else if((this.state.verdiep === "Verdiep-1" && (id >= 16 && id <= 60)) || (this.state.verdiep === "Verdiep-1" && id === 238)){
                this.drawBeacon(x * this.state.scale_x, y * this.state.scale_y, size, color)
            }else if((this.state.verdiep === "Verdiep-2" && id < 16) || (this.state.verdiep === "Verdiep-2" && id === 237)){
                this.drawBeacon(x * this.state.scale_x, y * this.state.scale_y, size, color)
            }else if(this.state.verdiep === "Verdiep1" && id >= 114 && id <= 160){
                this.drawBeacon(x * this.state.scale_x, y * this.state.scale_y, size, color)
            }else if(this.state.verdiep === "Verdiep2" && id >= 161 && id <= 204){
                this.drawBeacon(x * this.state.scale_x, y * this.state.scale_y, size, color)
            }else if(this.state.verdiep === "Verdiep3" && id >= 205 && id <= 236){
                this.drawBeacon(x * this.state.scale_x, y * this.state.scale_y, size, color)
            }
        }
        this.setState({ active: false })
    }

    calcClosestBeacon(){
        if(this.beacons.filter(b => b.rssi > Number.MIN_SAFE_INTEGER).length >= 4){
            this.setState({
                closestBeacons: this.beacons.sort(this.compare).slice(0, 4)
            }, () => {
                var ctx = this.refs.canvas.getContext("2d");
                ctx.clearRect(0, 0, this.refs.canvas.width, this.refs.canvas.height);
                ctx.beginPath();
                ctx.moveTo(this.state.closestBeacons[0].x * this.state.scale_x, this.state.closestBeacons[0].y * this.state.scale_y);
                ctx.lineTo(this.state.closestBeacons[1].x * this.state.scale_x, this.state.closestBeacons[1].y * this.state.scale_y);
                ctx.lineTo(this.state.closestBeacons[2].x * this.state.scale_x, this.state.closestBeacons[2].y * this.state.scale_y);
                ctx.lineTo(this.state.closestBeacons[3].x * this.state.scale_x, this.state.closestBeacons[3].y * this.state.scale_y);
                ctx.closePath();
                ctx.lineWidth = 50 * this.state.scale_x;
                ctx.lineJoin = "round";
                ctx.strokeStyle = "rgba(43, 196, 29, 0.3)";
                ctx.fillStyle = "rgba(43, 196, 29, 0)";
                ctx.fill();
                ctx.stroke();
                var closestBeacon = this.state.closestBeacons[0]
                this.setFloormap(closestBeacon.id);
            });
        }
    }

    setFloormap(id){
        var floor, verdieping
        if(id >= 205 && id <= 236){
            floor = verdiep_3
            verdieping = "Verdiep3"
        }else if(id >= 161 && id <= 204){
            floor = verdiep_2
            verdieping = "Verdiep2"
        }else if(id >= 114 && id <= 160){
            floor = verdiep_1 
            verdieping = "Verdiep1"
        }else if(id >= 61 && id <= 113){
            floor = verdiep_0
            verdieping = "Verdiep0"
        }else if((id >= 16 && id <= 60) || id === 238){
            floor = verdiep_min1
            verdieping = "Verdiep-1"
        }else if(id < 16 || id === 237){
            floor = verdiep_min2
            verdieping = "Verdiep-2"
        }
        this.setState({
            img: floor,
            verdiep: verdieping,
        }, () => {
            this.drawBeacons();
        })
    }
    drawBeacon(x, y, size, color){
        var ctx = this.refs.canvas.getContext("2d");
        if(color === "RED"){
            ctx.fillStyle = "#ff2626"; 
        }else if(color === "ORANGE"){
            ctx.fillStyle = "#ea8e15"
        }else if(color === "GREEN"){
            ctx.fillStyle = "#2bc41d";
        }
        ctx.beginPath();
        ctx.arc(x, y, size, 0, Math.PI * 2, true);
        ctx.fill();
    }

    componentWillReceiveProps(nextProps){
        var device = nextProps.device
        if(device){
            if(device.name && device.beacons){
                this.device = device.name;
                this.resetBeacons();
                for (var key in device.beacons) {
                    // eslint-disable-next-line
                    var obj = this.beacons.find(b => b.id === parseInt(key, 16))
                    if(obj){
                        obj.rssi = device.beacons[key]['RSSI']
                    }
                }
                this.configCanvas();
                this.calcClosestBeacon();
            }else{
                this.setState({
                    active: true
                })
            }
        }
    }

    resetBeacons(){
        for (var key in this.beacons) {
            this.beacons[key].rssi  = Number.MIN_SAFE_INTEGER;
        }
    }

    render(){
        return(
            <div>
                <Dimmer.Dimmable blurring dimmed={this.state.active}>
                <Dimmer active={this.state.active} inverted />
                    <img  
                        className="ui fluid image"
                        ref="verdiep" 
                        src={this.state.img} 
                        onLoad={this.handleOnLoadAndResize.bind(this)}
                        alt="verdieping"/>
                        <div 
                            className="wrapper"
                            width="0"
                            height="0"
                            ref="wrapper">
                            <h3 className="ui blue header">{this.state.verdiep}</h3>
                            <canvas 
                            ref="canvas" 
                            width="0" 
                            height="0">
                            </canvas>
                    </div>
                </Dimmer.Dimmable>
            </div>
        )
    }
}