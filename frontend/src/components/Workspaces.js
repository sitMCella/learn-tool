import React, { useState, useEffect } from 'react';
import Workspace from "./Workspace";
import {makeStyles, useTheme} from '@material-ui/core/styles';
import List from '@material-ui/core/List';
import { Button } from '@material-ui/core';
import MuiAlert from '@material-ui/lab/Alert';
import AppBar from '@material-ui/core/AppBar';
import Toolbar from '@material-ui/core/Toolbar';
import IconButton from '@material-ui/core/IconButton';
import MenuIcon from '@material-ui/icons/Menu';
import clsx from "clsx";
import ChevronRightIcon from "@material-ui/icons/ChevronRight";
import ChevronLeftIcon from "@material-ui/icons/ChevronLeft";
import Divider from "@material-ui/core/Divider";
import ListItem from "@material-ui/core/ListItem";
import {Link} from "react-router-dom";
import ListItemIcon from "@material-ui/core/ListItemIcon";
import DashboardIcon from '@material-ui/icons/Dashboard';
import ListItemText from "@material-ui/core/ListItemText";
import Drawer from "@material-ui/core/Drawer";
import UploadIcon from "@material-ui/icons/Publish";

function Alert(props) {
    return <MuiAlert elevation={6} variant="filled" {...props} />;
}

function Workspaces() {
    const [list, setList] = useState([]);
    const [newWorkspaceStatus, setNewWorkspaceStatus] = useState(false);
    const [workspaceError, setWorkspaceError] = useState(false);
    const [workspaceErrorMessage, setWorkspaceErrorMessage] = useState('');
    const getWorkspaces = async () => {
        const response = await fetch('/api/workspaces', {
            method: 'GET',
            headers: {
                'Accepted': 'application/json'
            }
        });
        const responseData = await response.json();
        const loadedWorkspaces = [];
        for (const key in responseData) {
            loadedWorkspaces.push({
                name: responseData[key].name,
                new: false
            })
        }
        setList(loadedWorkspaces);
    }
    useEffect(() => {
        getWorkspaces()
            .then(() => setWorkspaceError(false))
            .catch(() => {
                setWorkspaceError(true);
                setWorkspaceErrorMessage("Cannot retrieve the Workspaces, please refresh the page.")
            });
    }, []);
    const newWorkspaceHandler = () => {
        if(newWorkspaceStatus) {
            return;
        }
        const newWorkspaces = [{ name: "New Workspace Name", new: true }, ...list];
        setList(newWorkspaces);
        setNewWorkspaceStatus(true);
        setWorkspaceError(false);
    };
    const submitHandler = (workspaceName) => {
        const newWorkspaces = [{ name: workspaceName, new: false }, ...list.slice(1)];
        setList(newWorkspaces);
        setNewWorkspaceStatus(false);
    }
    const createErrorHandler = (errCode) => {
        const newWorkspaces = list.slice(1);
        setList(newWorkspaces);
        setNewWorkspaceStatus(false);
        setWorkspaceError(true);
        if(errCode === "422") {
            setWorkspaceErrorMessage("Cannot create the Workspace. The Workspace name should not contain spaces.");
        } else if(errCode === "409") {
            setWorkspaceErrorMessage("Cannot create the Workspace. The Workspace already exists.");
        } else {
            setWorkspaceErrorMessage("Cannot create the Workspace.");
        }
    }
    const cancelButtonClickHandler = () => {
        const newWorkspaces = list.slice(1);
        setList(newWorkspaces);
        setNewWorkspaceStatus(false);
    };
    const drawerWidth = 240;
    const [open, setOpen] = React.useState(false);
    const handleDrawerToggle = () => {
        setOpen(!open);
    };
    const handleDrawerClose = () => {
        setOpen(false);
    };
    const handleUploadFileData = (event) => {
        event.preventDefault();
        let data = new FormData();
        data.append('backup', event.target.files[0]);
        const importBackup = async () => {
            const response = await fetch('/api/workspaces/import', {
                method: 'POST',
                body: data
            });
            if(!response.ok) {
                setWorkspaceError(true);
                if(response.status === 409) {
                    setWorkspaceErrorMessage("Cannot import the Workspace. The Workspace already exists.");
                } else {
                    setWorkspaceErrorMessage("Cannot import the Workspace.");
                }
            } else {
                setWorkspaceError(false);
                getWorkspaces()
                    .then(() => setWorkspaceError(false))
                    .catch(() => {
                        setWorkspaceError(true);
                        setWorkspaceErrorMessage("Cannot retrieve the Workspaces, please refresh the page.")
                    })
                    .catch(() => {console.log("Error while importing the Workspace.")})
            }
            const responseData = await response;
        };
        importBackup();
    }
    const useStyles = makeStyles((theme) => ({
        menuButton: {
            marginRight: theme.spacing(2),
            ['@media only screen and (max-width:768px)']: {
                display: 'none',
            },
        },
        title: {
            marginRight: theme.spacing(10),
        },
        appBar: {
            ['@media only screen and (max-width:14000px)']: {
                marginLeft: theme.spacing(5),
            },
            marginBottom: theme.spacing(2),
            zIndex: theme.zIndex.drawer + 1,
            transition: theme.transitions.create(['width', 'margin'], {
                easing: theme.transitions.easing.sharp,
                duration: theme.transitions.duration.leavingScreen,
            }),
        },
        drawer: {
            width: drawerWidth,
            flexShrink: 0,
            whiteSpace: 'nowrap',
        },
        drawerOpen: {
            width: drawerWidth,
            transition: theme.transitions.create('width', {
                easing: theme.transitions.easing.sharp,
                duration: theme.transitions.duration.enteringScreen,
            }),
        },
        drawerClose: {
            transition: theme.transitions.create('width', {
                easing: theme.transitions.easing.sharp,
                duration: theme.transitions.duration.leavingScreen,
            }),
            overflowX: 'hidden',
            width: theme.spacing(7),
            [theme.breakpoints.up('sm')]: {
                width: theme.spacing(7),
            },
        },
        hide: {
            display: 'none',
        },
        toolbar: {
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'flex-end',
            padding: theme.spacing(0, 1),
            ...theme.mixins.toolbar,
        },
        drawerCloseButton: {
            ['@media only screen and (max-width:768px)']: {
                display: 'none',
            },
        },
        divider: {
            ['@media only screen and (max-width:768px)']: {
                display: 'none',
            },
        },
        content: {
            ['@media only screen and (max-width:14000px)']: {
                marginLeft: theme.spacing(5),
            },
        }
    }));
    const classes = useStyles();
    const theme = useTheme();
    return (
        <div>
            <AppBar position="static" className={classes.appBar}>
                <Toolbar>
                    <IconButton edge="start" color="inherit" aria-label="menu" onClick={handleDrawerToggle} className={classes.menuButton}>
                        <MenuIcon />
                    </IconButton>
                    <Button color="inherit" onClick={newWorkspaceHandler} disabled={newWorkspaceStatus}>New Workspace</Button>
                </Toolbar>
            </AppBar>
            <Drawer
                variant="permanent"
                className={clsx(classes.drawer, {
                    [classes.drawerOpen]: open,
                    [classes.drawerClose]: !open,
                })}
                classes={{
                    paper: clsx({
                        [classes.drawerOpen]: open,
                        [classes.drawerClose]: !open,
                    }),
                }}
            >
                <div className={classes.toolbar}>
                    <IconButton onClick={handleDrawerClose} className={clsx(classes.drawerCloseButton, {
                        [classes.hide]: !open,
                    })}>
                        {theme.direction === 'rtl' ? <ChevronRightIcon /> : <ChevronLeftIcon />}
                    </IconButton>
                </div>
                <Divider className={classes.divider} />
                <List>
                    <ListItem button key="Workspaces" component={Link} to={'/workspaces'}>
                        <ListItemIcon><DashboardIcon /></ListItemIcon>
                        <ListItemText primary="Workspaces" />
                    </ListItem>
                </List>
                <List>
                    <ListItem button key="Workspaces">
                        <input style={{display: 'none'}} id="import" type="file" onChange={handleUploadFileData} />
                        <label htmlFor="import">
                            <ListItemIcon><UploadIcon/></ListItemIcon>
                        </label>
                    </ListItem>
                </List>
            </Drawer>
            <div className={classes.content}>
                {workspaceError && (<Alert severity="error">{workspaceErrorMessage}</Alert>)}
                <List component="nav" aria-label="main mailbox folders">
                    {list.map(workspace => <Workspace key={workspace.name} name={workspace.name} selected={false} new={workspace.new} handleSubmit={submitHandler} handleError={createErrorHandler} handleCancel={cancelButtonClickHandler}></Workspace>)}
                </List>
            </div>
        </div>
    );
}

export default Workspaces;
