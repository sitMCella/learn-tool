import React, {useEffect, useState} from 'react';
import { useParams, Link } from 'react-router-dom';
import {Button} from "@material-ui/core";
import List from "@material-ui/core/List";
import Card from './Card';
import Toolbar from "@material-ui/core/Toolbar";
import IconButton from "@material-ui/core/IconButton";
import MenuIcon from "@material-ui/icons/Menu";
import AppBar from "@material-ui/core/AppBar";
import {makeStyles} from "@material-ui/core/styles";

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
        const newCards = [{ id: null, question: "Question", response: "Response", new: true }, ...list];
        setList(newCards);
        setNewCardStatus(true);
    };
    const submitHandler = (id, question, response) => {
        const newCards = [{ id: id, question: question, response: response, new: false }, ...list.slice(1)];
        setList(newCards);
        setNewCardStatus(false);
    }
    const createErrorHandler = () => {
        const newCards = list.slice(1);
        setList(newCards);
        setNewCardStatus(false);
    }
    const cancelButtonClickHandler = () => {
        const newCards = list.slice(1);
        setList(newCards);
        setNewCardStatus(false);
    };
    const useStyles = makeStyles((theme) => ({
        appBar: {
            marginBottom: theme.spacing(2),
        },
        menuButton: {
            marginRight: theme.spacing(2),
        },
        title: {
            marginRight: theme.spacing(10),
        },
    }));
    const classes = useStyles();
    return (
        <div>
            <AppBar position="static" className={classes.appBar}>
                <Toolbar>
                    <IconButton className={classes.menuButton} edge="start" color="inherit" aria-label="menu">
                        <MenuIcon />
                    </IconButton>
                    <Button color="inherit" component={Link} to={'/workspaces'}>Workspaces</Button>
                    <Button color="inherit" onClick={newCardHandler} disabled={newCardStatus}>New Card</Button>
                    <Button color="inherit" component={Link} to={'/workspaces/' + params.name + '/study'}>Study</Button>
                </Toolbar>
            </AppBar>
            <List component="nav" aria-label="main mailbox folders">
                {list.map(card => <Card key={card.question} workspaceName={params.name} question={card.question} response={card.response} selected={false} new={card.new} handleSubmit={submitHandler} handleError={createErrorHandler} handleCancel={cancelButtonClickHandler}></Card>)}
            </List>
        </div>
    );
}

export default WorkspaceDetails;
