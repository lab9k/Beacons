import React from 'react';
import { Menu, Dropdown } from 'semantic-ui-react'
import 'firebase/database';

export class List extends React.Component {

    constructor(props) {
        super(props);
        this.database = this.props.firebase.database().ref();
        this.state = {
            activeItem: null,
            disabled: true,
            devices: []
        }
        this.handleItemClick = this.handleItemClick.bind(this);
        this.handleChange = this.handleChange.bind(this);
    }

    componentDidMount() {
        const previewDevices = this.state.devices;
        this.database.on('child_added', snap => {
            previewDevices.push({text: snap.key, value: snap.key,  beacons: snap.val()})
            this.setState({
                devices: previewDevices,
                disabled: false
            }, () => {
                this.forceUpdate();
            })
        })
        this.handleFirebase();
    }

    handleFirebase(){
        this.database.on('child_added', snap => {            
            var obj = this.state.devices.find(d => d.value === snap.key)
            obj.beacons = snap.val();
        })
        this.database.on('child_removed', snap => {
            var obj = this.state.devices.find(d => d.value === snap.key)
            obj.beacons = snap.val();
        }) 
        this.database.on('child_changed', snap => {            
            var obj = this.state.devices.find(d => d.value === snap.key)
            obj.beacons = snap.val();
        })
    }

    handleItemClick(e, { name, beacons }) {
        this.setState({ activeItem: name })
        this.props.handleChangeDevice({name: name,  beacons: beacons});
    }

    handleChange(e, { value }) {
        this.setState({ activeItem: value })
        var beacons = this.state.devices.find(d => d.value === value).beacons;
        this.props.handleChangeDevice({name: value, beacons: beacons});
    }

    render() {
        return (
            <div>{this.props.dropdown ? (
                <Dropdown fluid selection options={this.state.devices} onChange={this.handleChange} placeholder='Selecteer een toestel' disabled={this.state.disabled}/>
            ) : (
                    <Menu color='blue' only='tablet' fluid pointing secondary vertical>
                        {this.state.devices.map((device) => {
                            return (
                                <Menu.Item beacons={device.beacons} name={device.text} active={this.state.activeItem === device.text} onClick={this.handleItemClick} />
                            )
                        })}
                    </Menu>
                )}
            </div>
        )
    }
}