import React, { useState } from 'react';
import ListItem from '@material-ui/core/ListItem';
import {default as CardUi} from '@material-ui/core/Card';
import CardContent from '@material-ui/core/CardContent';
import TextField from '@material-ui/core/TextField';
import { Button } from "@material-ui/core";
import Box from '@material-ui/core/Box';
import Typography from '@material-ui/core/Typography';

function Card(props) {
    const [newQuestion, setNewQuestion] = useState('');
    const [newResponse, setNewResponse] = useState('');
    const questionChangeHandler = (event) => {
        setNewQuestion(event.target.value)
    };
    const responseChangeHandler = (event) => {
        setNewResponse(event.target.value)
    };
    const submitHandler = (event) => {
        event.preventDefault();
        const createCard = async () => {
            const response = await fetch('/api/workspaces/' + props.workspaceName + '/cards', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify({workspaceName: props.workspaceName, question: newQuestion, response: newResponse})
            });
            if(!response.ok) {
                throw new Error("Error while creating the Card");
            }
            const card = await response.json();
            props.handleSubmit(card.id, card.question, card.response);
            setNewQuestion('');
            setNewResponse('');
            createLearnCard(card.id).catch((err) => {
                throw err;
            });
        };
        const createLearnCard = async (cardId) => {
            const response = await fetch('/api/workspaces/' + props.workspaceName + '/learn', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify({cardId: cardId})
            });
            if(!response.ok) {
                throw new Error("Error while creating the Card");
            }
        };
        createCard().catch((err) => {
            console.log(err);
            props.handleError();
            setNewQuestion('');
            setNewResponse('');
        });
    };
    if (props.new) {
        return (
            <ListItem button selected={props.selected} >
                <form onSubmit={submitHandler}>
                    <Box display="flex" justifyContent="flex-start" alignItems="center">
                        <Box>
                            <TextField id="new-card-question" label="New question" variant="outlined" value={newQuestion} onChange={questionChangeHandler} />
                        </Box>
                        <Box>
                            <TextField id="new-card-response" label="New response" variant="outlined" value={newResponse} onChange={responseChangeHandler} />
                        </Box>
                        <Box ml="1rem">
                            <Button variant="contained" color="primary" type="submit">Create</Button>
                        </Box>
                        <Box ml="1rem">
                            <Button variant="contained" color="secondary" onClick={props.handleCancel}>Cancel</Button>
                        </Box>
                    </Box>
                </form>
            </ListItem>
        );
    } else {
        return (
            <ListItem button selected={props.selected}>
                <CardUi>
                    <CardContent>
                        <Typography variant="body2" color="textSecondary" component="p">
                            Question: {props.question}
                        </Typography>
                        <Typography variant="body2" color="textSecondary" component="p">
                            Response: {props.response}
                        </Typography>
                    </CardContent>
                </CardUi>
            </ListItem>
        );
    }
}

export default Card;
