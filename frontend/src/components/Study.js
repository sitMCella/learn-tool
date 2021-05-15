import React, {useEffect, useState} from 'react';
import CardContent from "@material-ui/core/CardContent";
import Typography from "@material-ui/core/Typography";
import {default as CardUi} from "@material-ui/core/Card/Card";
import {Link, useParams} from "react-router-dom";
import {Button} from "@material-ui/core";
import Box from '@material-ui/core/Box';

function Study() {
    const params = useParams();
    const [cardId, setCardId] = useState('');
    const [cardQuestion, setCardQuestion] = useState('');
    const [cardResponse, setCardResponse] = useState('');
    const [flipButtonVisible, setFlipButtonVisible] = useState(true);
    const [responseVisibility, setResponseVisibility] = useState('none');
    const [evaluationButtonsVisible, setEvaluationButtonsVisible] = useState(false);
    const [noCardsLft, setNoCardsLeft] = useState(false);
    const getCard = async () => {
        const response = await fetch('http://localhost:8080/workspaces/' + params.name + '/learn', {
            method: 'GET',
            headers: {
                'Accepted': 'application/json'
            },
        });
        if(!response.ok) {
            throw new Error(response.status);
        }
        const card = await response.json();
        setCardId(card.id);
        setCardQuestion(card.question);
        setCardResponse(card.response);
    };
    useEffect(() => {
        getCard().catch((err) => {
            console.log("Error while retrieving a card from the Workspace " + params.name + " status: " + err.message);
            setNoCardsLeft(true);
            setCardId('');
            setCardQuestion('');
            setCardResponse('');
        });
    }, []);
    const flipCardHandler = () => {
        setFlipButtonVisible(false);
        setResponseVisibility("block");
        setEvaluationButtonsVisible(true);
    }
    const evaluateCardHandler = () => {
        const evaluateCard = async () => {
            const response = await fetch('http://localhost:8080/workspaces/' + params.name + '/learn', {
                method: 'PUT',
                headers: {
                    'Content-Type': 'application/json',
                    'Accepted': 'application/json'
                },
                body: JSON.stringify({cardId: cardId, quality: 0})
            });
            if(!response.ok) {
                throw new Error("Error while evaluating the Card with Id " + cardId);
            }
            setCardId('');
            setCardQuestion('');
            setCardResponse('');
        };
        evaluateCard().then(() => {
            setEvaluationButtonsVisible(false);
            setResponseVisibility("none");
            setFlipButtonVisible(true);
            getCard().catch((err) => {
                console.log("Error while retrieving a card from the Workspace " + params.name + " status: " + err.message);
                setNoCardsLeft(true);
                setCardId('');
                setCardQuestion('');
                setCardResponse('');
            })
        }).catch((err) => {
            console.log(err);
            setCardId('');
            setCardQuestion('');
            setCardResponse('');
        });
    }
    const qualityValues = [0, 1, 2, 3, 4, 5];
    return (
        <div>
            { !noCardsLft ? (
                <div>
                    <CardUi>
                        <CardContent>
                            <Typography variant="body2" color="textSecondary" component="p">
                                Question: {cardQuestion}
                            </Typography>
                            <Box component="span" display={responseVisibility}>
                                <Typography variant="body2" color="textSecondary" component="p" >
                                    Response: {cardResponse}
                                </Typography>
                            </Box>
                        </CardContent>
                    </CardUi>
                    <Box component="span" m={3}>
                        {flipButtonVisible && (<Button variant="contained" color="primary" onClick={flipCardHandler}>Flip</Button>)}
                        {evaluationButtonsVisible && (
                            <Box component="span" display={responseVisibility}>
                                {qualityValues.map((value) => {
                                    return <Button key={value} variant="contained" color="primary" onClick={() => evaluateCardHandler({value})}>{value}</Button>
                                })}
                            </Box>
                        )}
                    </Box>
                </div>
            ) :
            (
                <div>
                    <CardUi>
                        <CardContent>
                            <Typography variant="body2" color="textSecondary" component="p">
                                No cards left
                            </Typography>
                        </CardContent>
                    </CardUi>
                    <Box component="span" m={3}>
                        <Button variant="contained" color="primary" component={Link} to={'/workspaces/' + params.name + '/cards'}>Close</Button>
                    </Box>
                </div>
            )}
        </div>
    )
}

export default Study;
