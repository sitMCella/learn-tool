import React, { useState } from 'react'
import Box from '@material-ui/core/Box'
import { ColorPicker } from 'material-ui-color'
import Fab from '@material-ui/core/Fab'
import List from '@material-ui/core/List'
import ListItem from '@material-ui/core/ListItem'
import Grid from '@material-ui/core/Grid'
import MenuItem from '@material-ui/core/MenuItem'
import MenuList from '@material-ui/core/MenuList'
import Paper from '@material-ui/core/Paper'
import Typography from '@material-ui/core/Typography'
import { makeStyles } from '@material-ui/core/styles'
import CloseIcon from '@material-ui/icons/Close'

const StudySettings = (props) => {
  const displayPage = 'display'
  const [settingsPage, setSettingsPage] = useState(displayPage)
  const [studyCardQuestionBackgroundColor, setStudyCardQuestionBackgroundColor] = useState(props.settings.studyCardQuestionBackgroundColor)
  const [studyCardQuestionTextColor, setStudyCardQuestionTextColor] = useState(props.settings.studyCardQuestionTextColor)
  const [studyCardResponseBackgroundColor, setStudyCardResponseBackgroundColor] = useState(props.settings.studyCardResponseBackgroundColor)
  const [studyCardResponseTextColor, setStudyCardResponseTextColor] = useState(props.settings.studyCardResponseTextColor)
  const [selectedMenuIndex, setSelectedMenuIndex] = useState(0)
  const options = ['Display']

  const handleOpenMenu = (event, index) => {
    setSelectedMenuIndex(index)
    if (index === 0) {
      setSettingsPage(displayPage)
    }
  }

  const handlePickStudyCardQuestionBackgroundColor = (event) => {
    setStudyCardQuestionBackgroundColor('#' + event.hex)
    const settings = {
      ...props.settings,
      studyCardQuestionBackgroundColor: '#' + event.hex
    }
    props.handleSettingsUpdate(settings)
  }

  const handlePickStudyCardQuestionTextColor = (event) => {
    setStudyCardQuestionTextColor('#' + event.hex)
    const settings = {
      ...props.settings,
      studyCardQuestionTextColor: '#' + event.hex
    }
    props.handleSettingsUpdate(settings)
  }

  const handlePickStudyCardResponseBackgroundColor = (event) => {
    setStudyCardResponseBackgroundColor('#' + event.hex)
    const settings = {
      ...props.settings,
      studyCardResponseBackgroundColor: '#' + event.hex
    }
    props.handleSettingsUpdate(settings)
  }

  const handlePickStudyCardResponseTextColor = (event) => {
    setStudyCardResponseTextColor('#' + event.hex)
    const settings = {
      ...props.settings,
      studyCardResponseTextColor: '#' + event.hex
    }
    props.handleSettingsUpdate(settings)
  }

  const useStyles = makeStyles((theme) => ({
    content: {
      marginRight: 0,
      width: '100%'
    },
    drawerList: {
      padding: theme.spacing(0, 0)
    },
    settingsContent: {
      padding: theme.spacing(2, 0)
    },
    title: {
      flex: 0,
      position: 'absolute',
      alignItems: 'center',
      fontSize: 'x-large',
      padding: theme.spacing(0, 1)
    },
    paper: {
      marginRight: theme.spacing(2),
      '@media only screen and (max-width:768px)': {
        marginRight: theme.spacing(0)
      }
    },
    closeIcon: {
      marginLeft: 'auto',
      marginRight: theme.spacing(2)
    },
    settingsMenu: {
      padding: theme.spacing(0, 0)
    },
    settingsMenuContent: {
      padding: theme.spacing(0, 0),
      '@media only screen and (max-width:768px)': {
        display: 'inline-block'
      }
    },
    settingsConfiguration: {
      padding: theme.spacing(0, 5),
      '@media only screen and (max-width:768px)': {
        padding: theme.spacing(0, 1)
      }
    },
    settingsGridRow: {
      width: '100%',
      padding: theme.spacing(2, 0)
    },
    settingsGrid: {
      width: '100%'
    }
  }))
  const classes = useStyles()

  return (
        <div className={classes.content}>
          <List className={classes.drawerList}>
            <ListItem className={classes.drawerList}>
              <Box className={classes.title}>Settings</Box>
              <Box className={classes.closeIcon}>
                <Fab size="small" color="primary" aria-label="avatar" onClick={props.handleClose} aria-haspopup="true">
                  <CloseIcon />
                </Fab>
              </Box>
            </ListItem>
            <ListItem className={classes.settingsContent}>
              <Grid container spacing={0}>
                <Grid item className={classes.settingsMenu}>
                  <Paper className={classes.paper}>
                    {options.map((option, index) => (
                      <MenuList key={option} className={classes.settingsMenuContent}>
                          <MenuItem selected={index === selectedMenuIndex} onClick={(event) => handleOpenMenu(event, index)}>{option}</MenuItem>
                      </MenuList>
                    ))}
                  </Paper>
                </Grid>
                {
                    settingsPage === displayPage && (
                        <Grid item className={classes.settingsConfiguration}>
                            <Grid container spacing={1}>
                              <Grid container spacing={2} className={classes.settingsGridRow}>
                                  <Grid item className={classes.settingsGrid}>
                                    <Typography id="contrast-slider" gutterBottom>
                                      Card Question background color
                                    </Typography>
                                    <ColorPicker defaultValue={'#89CFF0'} value={studyCardQuestionBackgroundColor} onChange={handlePickStudyCardQuestionBackgroundColor} deferred />
                                  </Grid>
                              </Grid>
                              <Grid container spacing={2} className={classes.settingsGridRow}>
                                <Grid item className={classes.settingsGrid}>
                                  <Typography id="contrast-slider" gutterBottom>
                                    Card Question text color
                                  </Typography>
                                  <ColorPicker defaultValue={'#000000A6'} value={studyCardQuestionTextColor} onChange={handlePickStudyCardQuestionTextColor} deferred />
                                </Grid>
                              </Grid>
                            </Grid>
                            <Grid container spacing={2} className={classes.settingsGridRow}>
                              <Grid item className={classes.settingsGrid}>
                                <Typography id="contrast-slider" gutterBottom>
                                  Card Response background color
                                </Typography>
                                <ColorPicker defaultValue={'#FFFFFFFF'} value={studyCardResponseBackgroundColor} onChange={handlePickStudyCardResponseBackgroundColor} deferred />
                              </Grid>
                            </Grid>
                            <Grid container spacing={2} className={classes.settingsGridRow}>
                              <Grid item className={classes.settingsGrid}>
                                <Typography id="contrast-slider" gutterBottom>
                                  Card Response text color
                                </Typography>
                                <ColorPicker defaultValue={'#000000A6'} value={studyCardResponseTextColor} onChange={handlePickStudyCardResponseTextColor} deferred />
                              </Grid>
                            </Grid>
                        </Grid>
                    )
                }
              </Grid>
            </ListItem>
          </List>
        </div>
  )
}

export default StudySettings
