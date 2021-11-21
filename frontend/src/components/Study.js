import React, {useEffect, useState} from 'react';
import CardContent from "@material-ui/core/CardContent";
import Typography from "@material-ui/core/Typography";
import {default as CardUi} from "@material-ui/core/Card/Card";
import {Link, useParams} from "react-router-dom";
import {Button} from "@material-ui/core";
import Box from '@material-ui/core/Box';
import {makeStyles, useTheme} from "@material-ui/core/styles";
import Toolbar from "@material-ui/core/Toolbar";
import IconButton from "@material-ui/core/IconButton";
import MenuIcon from "@material-ui/icons/Menu";
import AppBar from "@material-ui/core/AppBar";
import clsx from "clsx";
import ChevronRightIcon from "@material-ui/icons/ChevronRight";
import ChevronLeftIcon from "@material-ui/icons/ChevronLeft";
import Divider from "@material-ui/core/Divider";
import List from "@material-ui/core/List";
import ListItem from "@material-ui/core/ListItem";
import ListItemIcon from "@material-ui/core/ListItemIcon";
import DashboardIcon from '@material-ui/icons/Dashboard';
import ListItemText from "@material-ui/core/ListItemText";
import Drawer from "@material-ui/core/Drawer";
import FilterNoneIcon from '@material-ui/icons/FilterNone';
import Grid from '@material-ui/core/Grid';

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
        const response = await fetch('/api/workspaces/' + params.name + '/learn', {
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
            const response = await fetch('/api/workspaces/' + params.name + '/learn/' + cardId, {
                method: 'PUT',
                headers: {
                    'Content-Type': 'application/json',
                    'Accepted': 'application/json'
                },
                body: JSON.stringify({quality: 0})
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
            width: '100%',
            ['@media only screen and (max-width:14000px)']: {
                marginLeft: theme.spacing(5),
            },
        },
        title: {
            marginRight: theme.spacing(10),
        },
        card: {
            minHeight: '40vh',
            width: '100%',
            textAlign: 'left',
        },
        cardContent: {
            textAlign: 'left',
            width: '100%',
        },
        appbarBottom: {
            alignItems: 'center',
            width: '100%',
        },
    }));
    const classes = useStyles();
    const theme = useTheme();
    return (
        <div>
            <AppBar position="static" className={classes.appBar}>
                <Toolbar>
                    <IconButton className={classes.menuButton} edge="start" color="inherit" aria-label="menu" onClick={handleDrawerToggle}>
                        <MenuIcon />
                    </IconButton>
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
                    <ListItem button key="Workspaces" component={Link} to={'/workspaces/' + params.name + '/cards'}>
                        <ListItemIcon><FilterNoneIcon /></ListItemIcon>
                        <ListItemText primary="Cards" />
                    </ListItem>
                </List>
            </Drawer>
            { !noCardsLft ? (
                <div className={classes.content}>
                    <CardUi className={classes.card}>
                        <CardContent className={classes.cardContent}>
                            <Box display="flex" flexWrap="wrap" p={0} m={1}>
                                <Box pr={2}>
                                    <Typography variant="body1" color="textSecondary" component="p" gutterBottom >
                                        <b>Question:</b>
                                    </Typography>
                                </Box>
                                <Box p={0}>
                                    <Typography variant="body1" color="textSecondary" component="p" gutterBottom >
                                        {cardQuestion}
                                    </Typography>
                                </Box>
                            </Box>
                            <Box display={responseVisibility}>
                                <Box display="flex" flexWrap="wrap" p={0} m={1}>
                                    <Box pr={2}>
                                        <Typography variant="body1" color="textSecondary" component="p" gutterBottom >
                                            <b>Response:</b>
                                        </Typography>
                                    </Box>
                                    <Box p={0}>
                                        <Typography variant="body1" color="textSecondary" component="p" gutterBottom >
                                            {cardResponse}
                                        </Typography>
                                    </Box>
                                </Box>
                            </Box>
                        </CardContent>
                    </CardUi>
                    <AppBar position="static" className={classes.appbarBottom}>
                        <Toolbar>
                            {flipButtonVisible && (<Button color="inherit" style={{maxWidth: '50vh', minWidth: '50vh'}} onClick={flipCardHandler}>Flip</Button>)}
                            {evaluationButtonsVisible && (
                                <Box component="span" display={responseVisibility}>
                                    {qualityValues.map((value) => {
                                        return <Button key={value} color="inherit" onClick={() => evaluateCardHandler({value})}>{value}</Button>
                                    })}
                                </Box>
                            )}
                        </Toolbar>
                    </AppBar>
                </div>
            ) :
            (
                <div className={classes.content}>
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
