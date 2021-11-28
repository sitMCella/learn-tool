import React, {useEffect, useState} from 'react';
import { useParams, Link } from 'react-router-dom';
import {Button} from "@material-ui/core";
import List from "@material-ui/core/List";
import Card from './Card';
import Toolbar from "@material-ui/core/Toolbar";
import IconButton from "@material-ui/core/IconButton";
import MenuIcon from "@material-ui/icons/Menu";
import AppBar from "@material-ui/core/AppBar";
import {makeStyles, useTheme} from "@material-ui/core/styles";
import Drawer from '@material-ui/core/Drawer';
import Divider from '@material-ui/core/Divider';
import ListItem from '@material-ui/core/ListItem';
import ListItemIcon from '@material-ui/core/ListItemIcon';
import ListItemText from '@material-ui/core/ListItemText';
import DashboardIcon from '@material-ui/icons/Dashboard';
import SaveAltIcon from '@material-ui/icons/SaveAlt';

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
    };
    const createCardCancelHandler = () => {
        const newCards = list.slice(1);
        setList(newCards);
        setNewCardStatus(false);
    };
    const createCardErrorHandler = () => {
        const newCards = list.slice(1);
        setList(newCards);
        setNewCardStatus(false);
    };
    const updateCardHandler = (cardId) => {
        setNewCardStatus(true);
        const newCards = list.map(card => (card.id === cardId ? {...card, change: true} : card));
        setList(newCards);
    };
    const updateCardCompleteHandler = (cardId, question, response) => {
        setNewCardStatus(false);
        const newCards = list.map(card => (card.id === cardId ? {...card, question: question, response: response, change: false} : card));
        setList(newCards);
    };
    const updateCardCancelHandler = (cardId) => {
        setNewCardStatus(false);
        const newCards = list.map(card => (card.id === cardId ? {...card, change: false} : card));
        setList(newCards);
    };
    const updateCardErrorHandler = (cardId, question, response) => {
        setNewCardStatus(true);
        const newCards = list.map(card => (card.id === cardId ? {...card, question: question, response: response, change: false} : card));
        setList(newCards);
    }
    const deleteCardCompleteHandler = (cardId) => {
        const tempList = list.map(card => { return card.id; });
        const index = list.map(card => { return card.id; }).indexOf(cardId);
        const newCards = [...list.slice(0, index), ...list.slice(index + 1)];
        setList(newCards);
    };
    const handleExport = () => {
        const exportBackup = async () => {
            const response = await fetch('/api/workspaces/' + params.name + '/export', {
                method: 'GET',
                headers: {
                    'Accepted': 'application/octet-stream'
                }
            });
            const responseData = await response.blob();
            let url = window.URL.createObjectURL(responseData);
            let a = document.createElement('a');
            a.href = url;
            a.download = 'backup.zip';
            a.click();
        };
        exportBackup();
    }
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
        toolbar: {
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'flex-end',
            padding: theme.spacing(0, 1),
            ...theme.mixins.toolbar,
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
    return (
        <div>
            <AppBar position="static" className={classes.appBar}>
                <Toolbar>
                    <IconButton className={classes.menuButton} edge="start" color="inherit" aria-label="menu">
                        <MenuIcon />
                    </IconButton>
                    <Button color="inherit" onClick={newCardHandler} disabled={newCardStatus}>New Card</Button>
                    <Button color="inherit" component={Link} to={'/workspaces/' + params.name + '/study'}>Study</Button>
                </Toolbar>
            </AppBar>
            <Drawer variant="permanent" anchor="left">
                <div className={classes.toolbar}>
                </div>
                <Divider className={classes.divider} />
                <List>
                    <ListItem button key="Workspaces" component={Link} to={'/workspaces'}>
                        <ListItemIcon><DashboardIcon /></ListItemIcon>
                    </ListItem>
                </List>
                <List>
                    <ListItem button key="Workspaces" onClick={handleExport}>
                        <ListItemIcon><SaveAltIcon /></ListItemIcon>
                    </ListItem>
                </List>
            </Drawer>
            <div className={classes.content}>
                <List component="nav" aria-label="main mailbox folders">
                    {list.map(card => <Card key={card.id} workspaceName={params.name} id={card.id} question={card.question} response={card.response} selected={false} new={card.new} change={card.change}
    handleCreateCard={createCardHandler} handleCreateCardCancel={createCardCancelHandler} handleUpdateCard={updateCardHandler} handleCraeteCardError={createCardErrorHandler}
    handleUpdateCardComplete={updateCardCompleteHandler} handleUpdateCardCancel={updateCardCancelHandler} handleUpdateCardError={updateCardErrorHandler}
    handleDeleteCardComplete={deleteCardCompleteHandler}/>)}
                </List>
            </div>
        </div>
    );
}

export default WorkspaceDetails;
