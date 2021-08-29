import React, {useEffect, useState} from 'react';
import { useParams, Link } from 'react-router-dom';
import {Button} from "@material-ui/core";
import List from "@material-ui/core/List";
import Card from './Card';
import clsx from 'clsx';
import Toolbar from "@material-ui/core/Toolbar";
import IconButton from "@material-ui/core/IconButton";
import MenuIcon from "@material-ui/icons/Menu";
import AppBar from "@material-ui/core/AppBar";
import {makeStyles, useTheme} from "@material-ui/core/styles";
import Drawer from '@material-ui/core/Drawer';
import ChevronLeftIcon from '@material-ui/icons/ChevronLeft';
import ChevronRightIcon from '@material-ui/icons/ChevronRight';
import Divider from '@material-ui/core/Divider';
import ListItem from '@material-ui/core/ListItem';
import ListItemIcon from '@material-ui/core/ListItemIcon';
import ListItemText from '@material-ui/core/ListItemText';
import DashboardIcon from '@material-ui/icons/Dashboard';

const WorkspaceDetails = () => {
    const params = useParams();
    const [list, setList] = useState([]);
    const [newCardStatus, setNewCardStatus] = useState(false);
    useEffect(() => {
        const getCards = async () => {
            const response = await fetch('/api/workspaces/' + params.name + '/cards', {
                method: 'GET',
                headers: {
                    'Accepted': 'application/json'
                }
            });
            const responseData = await response.json();
            const loadedCards = [];
            for (const key in responseData) {
                loadedCards.push({
                    id: responseData[key].id,
                    question: responseData[key].question,
                    response: responseData[key].response,
                    new: false
                })
            }
            setList(loadedCards);
        }
        getCards();
    }, []);
    const newCardHandler = () => {
        if(newCardStatus) {
            return;
        }
        const newCards = [{ id: null, question: "Question", response: "Response", new: true, change: false}, ...list];
        setList(newCards);
        setNewCardStatus(true);
    };
    const createCardHandler = (id, question, response) => {
        const newCards = [{ id: id, question: question, response: response, new: false, change: false}, ...list.slice(1)];
        setList(newCards);
        setNewCardStatus(false);
    }
    const createCardCancelHandler = () => {
        const newCards = list.slice(1);
        setList(newCards);
        setNewCardStatus(false);
    };
    const updateCardHandler = (cardId) => {
        setNewCardStatus(true);
        const newCards = list.map(card => (card.id === cardId ? {...card, change: true} : card));
        setList(newCards);
    }
    const updateCardCompleteHandler = (cardId, question, response) => {
        setNewCardStatus(false);
        const newCards = list.map(card => (card.id === cardId ? {...card, question: question, response: response, change: false} : card));
        setList(newCards);
    }
    const updateCardCancelHandler = (cardId) => {
        setNewCardStatus(false);
        const newCards = list.map(card => (card.id === cardId ? {...card, change: false} : card));
        setList(newCards);
    }
    const createCardErrorHandler = () => {
        const newCards = list.slice(1);
        setList(newCards);
        setNewCardStatus(false);
    }
    const drawerWidth = 240;
    const [open, setOpen] = React.useState(false);
    const handleDrawerToggle = () => {
        setOpen(!open);
    };
    const handleDrawerClose = () => {
        setOpen(false);
    };
    const useStyles = makeStyles((theme) => ({
        menuButton: {
            marginRight: theme.spacing(2),
            ['@media only screen and (max-width:768px)']: {
                display: 'none',
            },
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
            width: theme.spacing(7) + 1,
            [theme.breakpoints.up('sm')]: {
                width: theme.spacing(7) + 1,
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
                    <Button color="inherit" onClick={newCardHandler} disabled={newCardStatus}>New Card</Button>
                    <Button color="inherit" component={Link} to={'/workspaces/' + params.name + '/study'}>Study</Button>
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
            </Drawer>
            <div className={classes.content}>
                <List component="nav" aria-label="main mailbox folders">
                    {list.map(card => <Card key={card.id} workspaceName={params.name} id={card.id} question={card.question} response={card.response} selected={false} new={card.new} change={card.change} handleCreateCard={createCardHandler} handleCreateCardCancel={createCardCancelHandler} handleUpdateCard={updateCardHandler} handleUpdateCardComplete={updateCardCompleteHandler} handleUpdateCardCancel={updateCardCancelHandler} handleCraeteCardError={createCardErrorHandler}></Card>)}
                </List>
            </div>
        </div>
    );
}

export default WorkspaceDetails;
