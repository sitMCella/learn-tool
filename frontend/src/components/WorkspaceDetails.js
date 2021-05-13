import React, {useEffect, useState} from 'react';
import { useParams } from 'react-router-dom';
import {Button} from "@material-ui/core";
import List from "@material-ui/core/List";
import Card from './Card';

const WorkspaceDetails = () => {
    const params = useParams();
    const [list, setList] = useState([]);
    const [newCardStatus, setNewCardStatus] = useState(false);
    useEffect(() => {
        const getCards = async () => {
            const response = await fetch('http://localhost:8080/workspaces/' + params.name + '/cards', {
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
        const newCards = list.concat({ id: null, question: "Question", response: "Response", new: true });
        setList(newCards);
        setNewCardStatus(true);
    };
    const submitHandler = (id, question, response) => {
        const newCards = list.slice(0,-1).concat({ id: id, question: question, response: response, new: false });
        setList(newCards);
        setNewCardStatus(false);
    }
    const createErrorHandler = () => {
        const newCards = list.slice(0,-1);
        setList(newCards);
        setNewCardStatus(false);
    }
    const cancelButtonClickHandler = () => {
        const newCards = list.slice(0,-1);
        setList(newCards);
        setNewCardStatus(false);
    };
    return (
        <div>
            <Button variant="contained" color="primary" onClick={newCardHandler} disabled={newCardStatus}>New Card</Button>
            <List component="nav" aria-label="main mailbox folders">
                {list.map(card => <Card key={card.question} workspaceName={params.name} question={card.question} response={card.response} selected={false} new={card.new} handleSubmit={submitHandler} handleError={createErrorHandler} handleCancel={cancelButtonClickHandler}></Card>)}
            </List>
        </div>
    );
}

export default WorkspaceDetails;
